package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Projects;
import com.interswitch.apigateway.repository.MongoProjectsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/projects")
public class ProjectsController {

    private MongoProjectsRepository projectsRepository;

    public ProjectsController(MongoProjectsRepository clientResourceDB) {
        this.projectsRepository = clientResourceDB;
    }

    @GetMapping(produces = "application/json")
    private Flux<Projects> getAllProjects() {
        return projectsRepository.findAll();
    }

    @PostMapping (value = "/save", produces = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Projects> saveProjects(@Validated @RequestBody Projects project){
        return projectsRepository.save(project);
    }

    @GetMapping(value= "/{projectId}", produces = "application/json")
    private Mono<ResponseEntity<Projects>> findByProjectId(@Validated @PathVariable String projectId){
        return projectsRepository.findByProjectId(projectId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @PutMapping(value="/update",produces = "application/json")
    private Mono<Projects> updateProject(@Validated @RequestBody Projects project) {
        return projectsRepository.findByProjectId(project.getProjectId())
                .flatMap(existing -> {
                    existing.setProjectName(project.getProjectName());
                    existing.setEmail(project.getEmail());
                    return projectsRepository.save(existing);
                });
    }


    @DeleteMapping("/delete/{id}")
    private Mono<ResponseEntity<Void>> deleteProject(@PathVariable String id){
        try {
            return projectsRepository.deleteById(id)
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
        }
        catch (Exception e){
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

    }



}
