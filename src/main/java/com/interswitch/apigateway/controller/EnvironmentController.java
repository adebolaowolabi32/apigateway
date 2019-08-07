package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Environment;
import com.interswitch.apigateway.repository.MongoEnvironmentRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/environment")
public class EnvironmentController {

    private MongoEnvironmentRepository repository;

    public EnvironmentController(MongoEnvironmentRepository repository) {
        this.repository = repository;
    }

    @GetMapping(produces = "application/json")
    private Flux<Environment> getAll() {
        return repository.findAll();
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Environment> addConfiguration(@Validated @RequestBody Environment environment) {
        return repository.save(environment);
    }

    @GetMapping(value = "/{routeId}", produces = "application/json")
    private Mono<Environment> findByRouteId(@Validated @PathVariable String routeId) {
        return repository.findByRouteId(routeId)
                .switchIfEmpty(Mono.error(new NotFoundException("Route configuration does not exist")));
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<Environment> updateConfiguration(@Validated @RequestBody Environment environment) {
        return repository.findByRouteId(environment.getRouteId())
                .switchIfEmpty(Mono.error(new NotFoundException("Route Configuration does not exist")))
                .flatMap(existing -> {
                    existing.setSandbox(environment.getSandbox());
                    existing.setUat(environment.getUat());
                    return repository.save(existing);
                });
    }

    @DeleteMapping("/{routeId}")
    private Mono<ResponseEntity<Void>> delete(@PathVariable String routeId) {
        return repository.findByRouteId(routeId)
                .flatMap(environment -> repository.deleteById(environment.getId())
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}