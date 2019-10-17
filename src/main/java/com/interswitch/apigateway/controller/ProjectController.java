package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.ProductRequest;
import com.interswitch.apigateway.model.ProjectData;
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
@RequestMapping("/projects")
public class ProjectController {
    private ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping(produces = "application/json")
    private Flux<ProjectData> getAll(@RequestHeader HttpHeaders headers) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.getAllProjects(email);
    }

    @GetMapping(value = "/{projectId}", produces = "application/json")
    private Mono<ProjectData> findById(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.getProject(email, projectId);
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<ProjectData> create(@RequestHeader HttpHeaders headers, @Validated @RequestBody ProjectData projectData) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.createProject(email, projectData);
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<ProjectData> update(@RequestHeader HttpHeaders headers, @Validated @RequestBody ProjectData projectData) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.updateProject(email, projectData);
    }

    @GetMapping(value = "/{projectId}/credentials/{env}", produces = "application/json")
    private Mono<Client> getClientCredentials(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId, @Validated @PathVariable Env env) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.getClientCredentials(email, projectId, env);
    }

    @GetMapping(value = "/{projectId}/requested", produces = "application/json")
    private Flux<ProductRequest> GetRequestedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.getRequestedResources(email, projectId);
    }

    @PostMapping(value = "/{projectId}/requested", produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    private Mono<Void> SaveRequestedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId, @Validated @RequestBody Map<String, LinkedHashSet<String>> request) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.saveRequestedResources(email, projectId, request);
    }

    @GetMapping(value = "/{projectId}/approved", produces = "application/json")
    private Flux<ProductRequest> GetApprovedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.getApprovedResources(email, projectId);
    }

    @GetMapping(value = "/{projectId}/available", produces = "application/json")
    private Flux<ProductRequest> GetAvailableResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String email = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "email");
        return projectService.getAvailableResources(email, projectId);
    }
}
