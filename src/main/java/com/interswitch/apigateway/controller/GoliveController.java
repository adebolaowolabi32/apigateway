package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.PassportClient;
import com.interswitch.apigateway.model.Project;
import com.interswitch.apigateway.repository.MongoProductRepository;
import com.interswitch.apigateway.repository.MongoProjectRepository;
import com.interswitch.apigateway.service.PassportService;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static com.interswitch.apigateway.service.PassportService.buildPassportClientForEnvironment;
import static com.interswitch.apigateway.util.FilterUtil.*;


@RestController
@RequestMapping("/golive")
public class GoliveController {
    private MongoProjectRepository mongoProjectRepository;
    private MongoProductRepository mongoProductRepository;
    private PassportService passportService;

    public GoliveController(MongoProjectRepository mongoProjectRepository, MongoProductRepository mongoProductRepository, PassportService passportService) {
        this.mongoProjectRepository = mongoProjectRepository;
        this.mongoProductRepository = mongoProductRepository;
        this.passportService = passportService;
    }

    @PostMapping(value = "/approve/{projectId}", produces = "application/json")
    private Mono<Project> approveProductRequests(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        return mongoProjectRepository.findById(projectId).flatMap(project -> {
            return Mono.just(project);
        }).switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")));
    }

    @PostMapping(value = "/request/{projectId}", produces = "application/json")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    private Mono<Project> requestToGoLive(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String accessToken = getBearerToken(headers);
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(accessToken), "user_name");
        return mongoProjectRepository.findById(projectId).flatMap(project -> {
                    if (username.equals(project.getOwner())) {
                        PassportClient passportClient = buildPassportClientForEnvironment(project, Env.LIVE);
                        return passportService.createPassportClient(passportClient, accessToken, Env.LIVE)
                                .flatMap(createdClient -> {
                                    project.setClientId(createdClient.getClientId(), Env.LIVE);
                                    return Mono.empty();
                                }).then(mongoProjectRepository.save(project));
                    }
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not go live because you are not the owner of this project");

                }
        ).switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")));
    }

   /* @PostMapping(value = "/approve/{projectId}", produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    private Mono<Project> approveGoLiveResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId, @Validated @RequestBody Map<String, Object> request) {
        String accessToken = getBearerToken(headers);
        return mongoProjectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")))
                .flatMap(project -> {
                    try {
                        Set<String> resources = new LinkedHashSet<String>((ArrayList) (request.get("resources")));
                        if (!resources.isEmpty()) {
                            Set<String> api_resources = new LinkedHashSet<>();
                            Set<String> audiences = new LinkedHashSet<>();
                            return Flux.fromIterable(resources).flatMap(r -> {
                                project.getResource(r).ifPresentOrElse(resource -> {
                                   String resourcePermission = resource.getId() + "-" + resource.getMethod() + "/" + resource.getPath();
                                   audiences.addAll(resource.getProduct().getAudiences());
                                   project.removeResource(resource);
                                }, () -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more of the requested resources do not exist")));
                                return Mono.empty();
                            }).then(Mono.defer(() -> {
                                project.setAudiences(audiences);
                                String clientId = project.getClientId(Env.LIVE);
                                if (!clientId.isEmpty()) {
                                    PassportClient passportClient = buildPassportClientForEnvironment(project, Env.LIVE);
                                    passportClient.setClientId(clientId);
                                    Map<String, Object> additionalInformation = new LinkedHashMap<>();
                                    passportClient.setAdditionalInformation();
                                    return passportService.updatePassportClient(passportClient, accessToken, Env.LIVE)
                                            .then(mongoProjectRepository.save(project));
                                }
                                return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You cannot request for these resources because you do not have a Test Client on Passport");
                            }));
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources list cannot be empty"));
                    } catch (Exception e) {
                        Mono.error(e).log();
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resources list cannot be null"));
                    }
                });
    }*/

}
