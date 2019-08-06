package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.RouteConfig;
import com.interswitch.apigateway.repository.MongoRouteConfigRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/config")
public class RouteConfigController {

    private MongoRouteConfigRepository repository;

    public RouteConfigController(MongoRouteConfigRepository repository) {
        this.repository = repository;
    }

    @GetMapping(produces = "application/json")
    private Flux<RouteConfig> getAll() {
        return repository.findAll();
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<RouteConfig> addConfiguration(@Validated @RequestBody RouteConfig routeConfig) {
        return repository.save(routeConfig);
    }

    @GetMapping(value = "/{routeId}", produces = "application/json")
    private Mono<RouteConfig> findByRouteId(@Validated @PathVariable String routeId) {
        return repository.findByRouteId(routeId)
                .switchIfEmpty(Mono.error(new NotFoundException("Route configuration does not exist")));
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<RouteConfig> updateConfiguration(@Validated @RequestBody RouteConfig config) {
        return repository.findByRouteId(config.getRouteId())
                .switchIfEmpty(Mono.error(new NotFoundException("Route Configuration does not exist")))
                .flatMap(existing -> {
                    existing.setSandboxUri(config.getSandboxUri());
                    existing.setUatUri(config.getUatUri());
                    return repository.save(existing);
                });
    }

    @DeleteMapping("/{config}")
    private Mono<ResponseEntity<Void>> delete(@PathVariable String routeId) {
        return repository.findByRouteId(routeId)
                .flatMap(config -> repository.deleteById(config.getId())
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
