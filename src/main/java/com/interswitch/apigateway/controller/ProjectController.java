package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.*;
import com.interswitch.apigateway.repository.MongoProductRepository;
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

import static com.interswitch.apigateway.service.PassportService.buildPassportClientForEnvironment;
import static com.interswitch.apigateway.util.FilterUtil.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private MongoProjectRepository mongoProjectRepository;
    private MongoProductRepository mongoProductRepository;
    private MongoResourceRepository mongoResourceRepository;


    private PassportService passportService;

    public ProjectController(MongoProjectRepository mongoProjectRepository, MongoProductRepository mongoProductRepository, MongoResourceRepository mongoResourceRepository, PassportService passportService) {
        this.mongoProjectRepository = mongoProjectRepository;
        this.mongoProductRepository = mongoProductRepository;
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
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not view this project because you are not its owner");
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")));
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Project> create(@RequestHeader HttpHeaders headers, @Validated @RequestBody Project project) {
        String name = project.getName().trim().toLowerCase();
        return mongoProjectRepository.existsByName(name)
                .flatMap(exists -> {
                    if (exists) throw new ResponseStatusException(HttpStatus.CONFLICT, "Project already exists");
                    String accessToken = getBearerToken(headers);
                    String username = getClaimAsStringFromBearerToken(decodeBearerToken(accessToken), "user_name");
                    project.setName(name);
                    project.setOwner(username);
                    project.setClients(new LinkedHashMap<>());
                    PassportClient passportClient = buildPassportClientForEnvironment(project, Env.TEST);
                    return passportService.createPassportClient(passportClient, accessToken, Env.TEST)
                            .flatMap(createdClient -> {
                                project.setClientId(createdClient.getClientId(), Env.TEST);
                                return mongoProjectRepository.save(project);
                            });
                });
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    private Mono<Project> update(@RequestHeader HttpHeaders headers, @Validated @RequestBody Project project) {
        String accessToken = getBearerToken(headers);
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(accessToken), "user_name");
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
                            return passportService.updatePassportClient(testPassportClient, accessToken, Env.TEST)
                                    .then(Mono.defer(() -> {
                                        if (!liveClientId.isEmpty()) {
                                            PassportClient livePassportClient = buildPassportClientForEnvironment(project, Env.LIVE);
                                            livePassportClient.setClientId(liveClientId);
                                            return passportService.updatePassportClient(livePassportClient, accessToken, Env.LIVE)
                                                    .then(mongoProjectRepository.save(project));
                                        }
                                        return mongoProjectRepository.save(project);
                                    }));
                        }
                        return Mono.empty();
                    } else {
                        throw new ResponseStatusException(HttpStatus.NOT_MODIFIED, "You can not modify this project because you are not its owner");
                    }
                });
    }

    @GetMapping(value = "/{projectId}/credentials/{env}", produces = "application/json")
    private Mono<Client> getClientCredentials(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId, @Validated @PathVariable Env env) {
        String accessToken = getBearerToken(headers);
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(accessToken), "user_name");
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    if (username.equals(project.getOwner())) {
                        Client client = new Client();
                        client.setClientId("");
                        client.setClientSecret("");
                        String clientId = project.getClientId(env);
                        if (!clientId.isEmpty()) {
                            return passportService.getPassportClient(clientId, accessToken, env)
                                    .flatMap(passportClient -> {
                                        client.setClientId(passportClient.getClientId());
                                        client.setClientSecret(passportClient.getClientSecret());
                                        return Mono.just(client);
                                    });
                        }
                        return Mono.just(client);
                    }
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not view credentials for this project because you are not its owner");
                });
    }

    @GetMapping(value = "/{projectId}/requested", produces = "application/json")
    private Mono<List<Map<String, Object>>> GetRequestedResources(@Validated @PathVariable String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .map(project -> {
                    List<Map<String, Object>> requested = new ArrayList<>();
                    Map<String, Object> requestedProduct = new LinkedHashMap<>();
                    Map<String, String> requestedResource = new LinkedHashMap<>();
                    Set<String> resources = project.getResources();
                    for (var r : resources) {
                        mongoResourceRepository.findById(r).flatMap(resource -> {
                            Product product = resource.getProduct();
                            requestedResource.put("id", resource.getId());
                            requestedResource.put("name", resource.getName());
                            requestedProduct.put("name", product.getName());
                            requestedProduct.put("description", product.getDescription());
                            requestedProduct.put("resources", requestedResource);
                            requested.add(requestedProduct);
                            return
                        });
                        requested.put();
                    }
                });
    }

    @PostMapping(value = "/{projectId}/requested", produces = "application/json", consumes = "application/json")
    private Mono<Project> SaveRequestedResources(@Validated @PathVariable String projectId) {
        return mongoProjectRepository.findById(projectId).flatMap(project ->
                mongoProductRepository.findAll().flatMap(product -> {
                    if (!product.getProjects().contains(project)) {
                        product.addProject(project);
                    }
                    return mongoProductRepository.save(product).flatMap(p -> {
                        return mongoProjectRepository.save(project);
                    });
                }).switchIfEmpty(Mono.error(new NotFoundException("Product does not exist")))
        ).switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")));
    }

    @GetMapping(value = "/{projectId}/products/approved", produces = "application/json")
    private Mono<Map<String, Object>> GetApprovedResources(@Validated @PathVariable String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .map(project -> project.getResources());
    }

    @GetMapping(value = "/{projectId}/products/available", produces = "application/json")
    private Mono<Map<String, Object>> GetAvailableResources(@Validated @PathVariable String projectId) {
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .map(project -> project.getResources());
    }

   /*@DeleteMapping("/{projectId}")
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
