package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ClientResourcesRepository;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
public class ClientResourcesController {

    private MongoClientResourcesRepository clientResourceDB;
    private ClientResourcesRepository clientResourceCache;

    public ClientResourcesController(MongoClientResourcesRepository clientResourceDB, ClientResourcesRepository clientResourceCache) {
        this.clientResourceDB = clientResourceDB;
        this.clientResourceCache = clientResourceCache;
    }

    @GetMapping(produces = "application/json")
    private Flux<ClientResources> getAllClientResources() {

        return clientResourceCache.findAll()
                    .map(clientResources -> clientResources.getValue());
    }

    @PostMapping (value = "/save", produces = "application/json")
    private Mono<ClientResources> saveClientResource(@Validated @RequestBody ClientResources clientResource){
        return clientResourceDB.save(clientResource)
                .filter(clientResources -> true)
                .then(clientResourceCache.save(clientResource));
    }

    @GetMapping(value= "/{clientId}", produces = "application/json")
    private Mono<ResponseEntity<ClientResources>> findByClientId(@Validated @PathVariable String clientId){
        return clientResourceCache.findByClientId(clientId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @PutMapping(value="/update",produces = "application/json")
    private Mono<ClientResources> updateClientResources(@Validated @RequestBody ClientResources clientResource) {
        return clientResourceDB.findByClientId(clientResource.getClientId())
                .flatMap(existing -> {
                    existing.setResourceIds(clientResource.getResourceIds());
                    return clientResourceDB.save(existing);
                }).doOnSuccess(c -> clientResourceCache.update(clientResource));
    }


    @DeleteMapping("/delete/{clientId}")
    private Mono<ResponseEntity<Void>> deleteClientResources(@PathVariable String clientId){
        try {
            return clientResourceDB.deleteByClientId(clientId)
                    .then(clientResourceCache.deleteByClientId(clientId))
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
        }
        catch (Exception e){
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
    }



}
