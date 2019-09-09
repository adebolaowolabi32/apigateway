package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.PassportClient;
import com.interswitch.apigateway.model.Project;
import com.interswitch.apigateway.repository.MongoProductRepository;
import com.interswitch.apigateway.repository.MongoProjectRepository;
import com.interswitch.apigateway.service.PassportService;
import com.nimbusds.jwt.JWT;
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
    private Mono<Project> requestToGoLive(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String accesstoken = getBearerToken(headers);
        JWT token = decodeBearerToken(accesstoken);
        String username = (token != null) ? getClaimAsStringFromBearerToken(token, "user_name") : "";
        return mongoProjectRepository.findById(projectId).flatMap(project -> {
                    if (username.equals(project.getOwner())) {
                        PassportClient passportClient = buildPassportClientForEnvironment(project, Env.LIVE);
                        return passportService.createPassportClient(passportClient, accesstoken, Env.LIVE)
                                .flatMap(createdClient -> {
                                    project.setClientId(createdClient.getClientId(), Env.LIVE);
                                    return Mono.empty();
                                }).then(mongoProjectRepository.save(project));
                    }
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You can not go live because you are not the owner of this project");

                }
        ).switchIfEmpty(Mono.error(new NotFoundException("Project does not exist")));
    }
}
