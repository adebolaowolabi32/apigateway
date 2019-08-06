package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Config;
import com.interswitch.apigateway.repository.MongoConfigRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/config")
public class ConfigController {

    private MongoConfigRepository repository;

    public ConfigController(MongoConfigRepository repository) {
        this.repository = repository;
    }

    @GetMapping(produces = "application/json")
    private Flux<Config> getAll() {
        return repository.findAll();
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Config> addConfiguration(@Validated @RequestBody Config config) {
        return repository.save(config);
    }

    @GetMapping(value = "/{routeId}", produces = "application/json")
    private Mono<Config> findByRouteId(@Validated @PathVariable String routeId) {
        return repository.findByRouteId(routeId)
                .switchIfEmpty(Mono.error(new NotFoundException("Route configuration does not exist")));
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<Config> updateConfiguration(@Validated @RequestBody Config config) {
        return repository.findByRouteId(config.getRouteId())
                .switchIfEmpty(Mono.error(new NotFoundException("Route Configuration does not exist")))
                .flatMap(existing -> {
                    existing.setSandbox(config.getSandbox());
                    existing.setUat(config.getUat());
                    return repository.save(existing);
                });
    }

    @DeleteMapping("/{routeId}")
    private Mono<ResponseEntity<Void>> delete(@PathVariable String routeId) {
        return repository.findByRouteId(routeId)
                .flatMap(config -> repository.deleteById(config.getId())
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
