package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.*;
import com.interswitch.apigateway.repository.MongoProjectRepository;
import com.interswitch.apigateway.repository.MongoResourceRepository;
import com.interswitch.apigateway.service.PassportService;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.interswitch.apigateway.service.PassportService.buildPassportClientForEnvironment;
import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private MongoProjectRepository mongoProjectRepository;
    private MongoResourceRepository mongoResourceRepository;


    private PassportService passportService;

    public ProjectController(MongoProjectRepository mongoProjectRepository, MongoResourceRepository mongoResourceRepository, PassportService passportService) {
        this.mongoProjectRepository = mongoProjectRepository;
        this.mongoResourceRepository = mongoResourceRepository;
        this.passportService = passportService;
    }

    @GetMapping(produces = "application/json")
    private Flux<Project> getAll(@RequestHeader HttpHeaders headers) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return mongoProjectRepository.findByOwner(username);
    }

    @GetMapping(value = "/{projectId}", produces = "application/json")
    private Mono<Project> findById(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return mongoProjectRepository.findById(projectId)
                .flatMap(project -> {
                    if (username.equals(project.getOwner()))
                        return Mono.just(project);
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not view this project because you are not its owner"));
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")));
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Project> create(@RequestHeader HttpHeaders headers, @Validated @RequestBody Project project) {
        String name = project.getName().trim().toLowerCase();
        return mongoProjectRepository.existsByName(name)
                .flatMap(exists -> {
                    if (exists)
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Project already exists"));
                    String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
                    project.setName(name);
                    project.setOwner(username);
                    project.setClients(new LinkedHashMap<>());
                    PassportClient passportClient = buildPassportClientForEnvironment(project, Env.TEST);
                    return passportService.createPassportClient(passportClient, Env.TEST)
                            .flatMap(createdClient -> {
                                project.setClientId(createdClient.getClientId(), Env.TEST);
                                return mongoProjectRepository.save(project);
                            });
                });
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    private Mono<Project> update(@RequestHeader HttpHeaders headers, @Validated @RequestBody Project project) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return mongoProjectRepository.findById(project.getId())
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(existing -> {
                    if (username.equals(existing.getOwner())) {
                        project.setId(existing.getId());
                        project.setName(project.getName().trim().toLowerCase());
                        project.setOwner(existing.getOwner());
                        project.setClients(existing.getClients());
                        String testClientId = project.getClientId(Env.TEST);
                        String liveClientId = project.getClientId(Env.LIVE);

                        if (!testClientId.isEmpty()) {
                            PassportClient testPassportClient = buildPassportClientForEnvironment(project, Env.TEST);
                            testPassportClient.setClientId(testClientId);
                            return passportService.updatePassportClient(testPassportClient, Env.TEST)
                                    .then(Mono.defer(() -> {
                                        if (!liveClientId.isEmpty()) {
                                            PassportClient livePassportClient = buildPassportClientForEnvironment(project, Env.LIVE);
                                            livePassportClient.setClientId(liveClientId);
                                            return passportService.updatePassportClient(livePassportClient, Env.LIVE)
                                                    .then(mongoProjectRepository.save(project));
                                        }
                                        return mongoProjectRepository.save(project);
                                    }));
                        }
                        return Mono.empty();
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_MODIFIED, "You can not modify this project because you are not its owner"));
                    }
                });
    }

    @GetMapping(value = "/{projectId}/credentials/{env}", produces = "application/json")
    private Mono<Client> getClientCredentials(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId, @Validated @PathVariable Env env) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (username.equals(project.getOwner())) {
                        Client client = new Client();
                        client.setClientId("");
                        client.setClientSecret("");
                        String clientId = project.getClientId(env);
                        if (!clientId.isEmpty()) {
                            return passportService.getPassportClient(clientId, env)
                                    .flatMap(passportClient -> {
                                        client.setClientId(passportClient.getClientId());
                                        client.setClientSecret(passportClient.getClientSecret());
                                        return Mono.just(client);
                                    });
                        }
                        return Mono.just(client);
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not view credentials for this project because you are not its owner"));
                });
    }

    @GetMapping(value = "/{projectId}/requested", produces = "application/json")
    private Mono<List<Project.Product>> GetRequestedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        List<Project.Product> requested = new ArrayList<>();
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (username.equals(project.getOwner())) {
                        Set<Resource> resources = project.getResources();
                        return Flux.fromIterable(resources).flatMap(resource -> {
                            Product product = resource.getProduct();
                            Project.Product requestedProduct = new Project.Product();
                            Project.Resource requestedResource = new Project.Resource();
                            requestedResource.setId(resource.getId());
                            requestedResource.setName(resource.getName());
                            requestedProduct.setName(product.getName());
                            requestedProduct.setDescription(product.getDescription());
                            AtomicBoolean exists = new AtomicBoolean(false);
                            for (var request : requested) {
                                if (request.getName().equalsIgnoreCase(product.getName())) {
                                    Set<Project.Resource> listOfRequestedResources = request.getResources();
                                    listOfRequestedResources.add(requestedResource);
                                    request.setResources(listOfRequestedResources);
                                    exists.set(true);
                                    break;
                                }
                            }

                            if (!exists.get()) {
                                Set<Project.Resource> listOfRequestedResources = new LinkedHashSet<>();
                                listOfRequestedResources.add(requestedResource);
                                requestedProduct.setResources(listOfRequestedResources);
                                requested.add(requestedProduct);
                            }
                            return Mono.empty();
                        }).then();
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not view these resources because you are not the owner of this project"));
                }).then(Mono.just(requested));
    }

    @PostMapping(value = "/{projectId}/requested", produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    private Mono<Project> SaveRequestedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId, @Validated @RequestBody Map<String, Object> request) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (username.equals(project.getOwner())) {
                        Set<String> resources = new LinkedHashSet<>();
                        var resourcesObj = request.get("resources");
                        if (resourcesObj instanceof ArrayList)
                            resources = new LinkedHashSet<>((ArrayList) resourcesObj);
                        if (!resources.isEmpty()) {
                            project.setResources(new LinkedHashSet<>());
                            Set<String> audiences = new LinkedHashSet<>();
                            return Flux.fromIterable(resources).flatMap(r -> {
                                return mongoResourceRepository.findById(r)
                                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more of the requested resources do not exist")))
                                        .flatMap(resource -> {
                                            project.addResource(resource);
                                            audiences.addAll(resource.getProduct().getAudiences());
                                            return Mono.empty();
                                        });
                            }).then(Mono.defer(() -> {
                                project.setAudiences(audiences);
                                String clientId = project.getClientId(Env.TEST);
                                if (!clientId.isEmpty()) {
                                    PassportClient passportClient = buildPassportClientForEnvironment(project, Env.TEST);
                                    passportClient.setClientId(clientId);
                                    return passportService.updatePassportClient(passportClient, Env.TEST)
                                            .then(mongoProjectRepository.save(project));
                                }
                                return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You cannot request for these resources because you do not have a Test Client on Passport"));
                            }));
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources list cannot be null or empty"));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not request for these resources because you are not the owner of this project"));
                });
    }

    @GetMapping(value = "/{projectId}/approved", produces = "application/json")
    private Mono<List<Project.Product>> GetApprovedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        List<Project.Product> listofApproved = new ArrayList<>();
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (username.equals(project.getOwner())) {
                        String clientId = project.getClientId(Env.LIVE);
                        if (!clientId.isEmpty()) {
                            return passportService.getPassportClient(clientId, Env.LIVE).flatMap(passportClient -> {
                                Map<String, Object> additionalInformation = passportClient.getAdditionalInformation();
                                Set<String> api_resources = new LinkedHashSet<>();
                                var api_resourcesObj = additionalInformation.get("api_resources");
                                if (api_resourcesObj instanceof ArrayList)
                                    api_resources = new LinkedHashSet<>((ArrayList) api_resourcesObj);
                                return Flux.fromIterable(api_resources).flatMap(api_resource -> {
                                    String resourceId = api_resource.substring(0, api_resource.indexOf("-"));
                                    if (!resourceId.isEmpty()) {
                                        return mongoResourceRepository.findById(resourceId).flatMap(resource -> {
                                            Product product = resource.getProduct();
                                            Project.Product approvedProduct = new Project.Product();
                                            Project.Resource approvedResource = new Project.Resource();
                                            approvedResource.setId(resource.getId());
                                            approvedResource.setName(resource.getName());
                                            approvedProduct.setName(product.getName());
                                            approvedProduct.setDescription(product.getDescription());
                                            AtomicBoolean exists = new AtomicBoolean(false);
                                            for (var approved : listofApproved) {
                                                if (approved.getName().equalsIgnoreCase(product.getName())) {
                                                    Set<Project.Resource> listOfApprovedResources = approved.getResources();
                                                    listOfApprovedResources.add(approvedResource);
                                                    approved.setResources(listOfApprovedResources);
                                                    exists.set(true);
                                                    break;
                                                }
                                            }

                                            if (!exists.get()) {
                                                Set<Project.Resource> listOfApprovedResources = new LinkedHashSet<>();
                                                listOfApprovedResources.add(approvedResource);
                                                approvedProduct.setResources(listOfApprovedResources);
                                                listofApproved.add(approvedProduct);
                                            }
                                            return Mono.empty();
                                        });
                                    }
                                    return Mono.empty();
                                }).then();
                            });
                        }
                        return Mono.empty();
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not view these resources because you are not the owner of this project"));
                }).then(Mono.just(listofApproved));
    }
    /*@GetMapping(value = "/{projectId}/products/approved", produces = "application/json")
    private Mono<Map<String, Object>> GetApprovedResources(@Validated @PathVariable String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .map(project -> project.getResources());
    }*/


   /*@GetMapping(value = "/{projectId}/products/available", produces = "application/json")
    private Mono<Map<String, Object>> GetAvailableResources(@Validated @PathVariable String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .map(project -> project.getResources());
    }

   @DeleteMapping("/{projectId}")
    private Mono<ResponseEntity<Void>> delete(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String accessToken = getBearerToken(headers);
        JWT token = decodeBearerToken(accessToken);
        String username = (token != null) ? getClaimAsStringFromBearerToken(token, "user_name") : "";
        return mongoProjectRepository.findById(projectId)
                .flatMap(project -> {
                    if (username.equals(project.getOwner()))
                        return mongoProjectRepository.deleteById(project.getId()).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not delete this project because you are not its owner");
                }).switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }*/
}
