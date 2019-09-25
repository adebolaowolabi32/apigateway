package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Project;
import com.interswitch.apigateway.service.ProjectService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.Map;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;


@RestController
@RequestMapping("/golive")
public class GoliveController {

    private ProjectService projectService;

    public GoliveController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping(value = "/request/{projectId}", produces = "application/json")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    private Mono<Void> requestToGoLive(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.requestProjectGoLive(username, projectId);
    }

    @PostMapping(value = "/approve/{projectId}", produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    private Mono<Void> approveGoLiveResources(@Validated @PathVariable String projectId, @Validated @RequestBody Map<String, LinkedHashSet<String>> request) {
        return projectService.saveApprovedResources(projectId, request);
    }

    @PostMapping(value = "/decline/{projectId}", produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    private Mono<Void> declineGoLiveResources(@Validated @PathVariable String projectId, @Validated @RequestBody Map<String, LinkedHashSet<String>> request) {
        return projectService.declineRequestedResources(projectId, request);
    }

    @GetMapping(value = "/pending", produces = "application/json")
    private Flux<Project> GetPendingProjects() {
        return projectService.getPendingProjects();
    }
}
