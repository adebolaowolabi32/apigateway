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
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.getAllProjects(username);
    }

    @GetMapping(value = "/{projectId}", produces = "application/json")
    private Mono<ProjectData> findById(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.getProject(username, projectId);
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<ProjectData> create(@RequestHeader HttpHeaders headers, @Validated @RequestBody ProjectData projectData) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.createProject(username, projectData);
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<ProjectData> update(@RequestHeader HttpHeaders headers, @Validated @RequestBody ProjectData projectData) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.updateProject(username, projectData);
    }

    @GetMapping(value = "/{projectId}/credentials/{env}", produces = "application/json")
    private Mono<Client> getClientCredentials(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId, @Validated @PathVariable Env env) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.getClientCredentials(username, projectId, env);
    }

    @GetMapping(value = "/{projectId}/requested", produces = "application/json")
    private Flux<ProductRequest> GetRequestedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.getRequestedResources(username, projectId);
    }

    @PostMapping(value = "/{projectId}/requested", produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    private Mono<Void> SaveRequestedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId, @Validated @RequestBody Map<String, LinkedHashSet<String>> request) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.saveRequestedResources(username, projectId, request);
    }

    @GetMapping(value = "/{projectId}/approved", produces = "application/json")
    private Flux<ProductRequest> GetApprovedResources(@RequestHeader HttpHeaders headers, @Validated @PathVariable String projectId) {
        String username = getClaimAsStringFromBearerToken(decodeBearerToken(headers), "user_name");
        return projectService.getApprovedResources(username, projectId);
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
