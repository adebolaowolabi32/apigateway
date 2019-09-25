package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.RouteEnvironment;
import com.interswitch.apigateway.repository.MongoRouteEnvironmentRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/env")
public class RouteEnvironmentController {

    private MongoRouteEnvironmentRepository repository;

    public RouteEnvironmentController(MongoRouteEnvironmentRepository repository) {
        this.repository = repository;
    }

    @GetMapping(produces = "application/json")
    private Flux<RouteEnvironment> getAll() {
        return repository.findAll();
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<RouteEnvironment> addConfiguration(@Validated @RequestBody RouteEnvironment routeEnvironment) {
        String routeId = routeEnvironment.getRouteId().trim();
        return repository.existsByRouteId(routeId)
                .flatMap(exists -> {
                    if (exists)
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Route environment configuration already exists");
                    routeEnvironment.setRouteId(routeId);
                    return repository.save(routeEnvironment);
                });
    }

    @GetMapping(value = "/{routeId}", produces = "application/json")
    private Mono<RouteEnvironment> findByRouteId(@Validated @PathVariable String routeId) {
        return repository.findByRouteId(routeId)
                .switchIfEmpty(Mono.error(new NotFoundException("Route environment configuration does not exist")));
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<RouteEnvironment> updateConfiguration(@Validated @RequestBody RouteEnvironment routeEnvironment) {
        return repository.findByRouteId(routeEnvironment.getRouteId())
                .switchIfEmpty(Mono.error(new NotFoundException("Route environment configuration does not exist")))
                .flatMap(existing -> {
                    routeEnvironment.setId(existing.getId());
                    return repository.save(routeEnvironment);
                });
    }

    @DeleteMapping("/{routeId}")
    private Mono<ResponseEntity<Void>> delete(@Validated @PathVariable String routeId) {
        return repository.findByRouteId(routeId)
                .flatMap(env -> repository.deleteById(env.getId())
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}