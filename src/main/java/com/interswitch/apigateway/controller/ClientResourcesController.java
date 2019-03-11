package com.interswitch.apigateway.controller;


import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/resources")
public class ClientResourcesController {

    private ClientMongoRepository clientResourceDB;
    private ClientCacheRepository clientResourceCache;

    public ClientResourcesController(ClientMongoRepository clientResourceDB, ClientCacheRepository clientResourceCache) {
        this.clientResourceDB = clientResourceDB;
        this.clientResourceCache = clientResourceCache;
    }

    @GetMapping(produces = "application/json")
    private Flux<Client> getAllClientResources() {

        return clientResourceCache.findAll()
                .map(clientResources -> clientResources.getValue());
    }

    @PostMapping (value = "/save", produces = "application/json")
    private Mono<ResponseEntity<Client>> saveClientResource(@Validated @RequestBody Client client){
        return clientResourceDB.findByClientId(client.getClientId())
                .flatMap(existing -> Mono.error(new RuntimeException("Client Resource already exists")))
                .switchIfEmpty(clientResourceDB.save(client).then(clientResourceCache.save(client)))
                .then(Mono.defer(() -> Mono.just(ResponseEntity.created(URI.create("/routes/"+client.getClientId())).build())));
    }
    @GetMapping(value= "/{clientId}", produces = "application/json")
    private Mono<ResponseEntity<Client>> findByClientId(@Validated @PathVariable String clientId){
        return clientResourceCache.findByClientId(clientId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @PutMapping(value="/update",produces = "application/json")
    private Mono<ResponseEntity<Client>> updateClientResources(@Validated @RequestBody Client client) {
        return clientResourceDB.findByClientId(client.getClientId())
                .flatMap(existing -> {
                    existing.setResourceIds(client.getResourceIds());
                    return clientResourceDB.save(existing)
                            .then(clientResourceCache.update(client))
                            .map(ResponseEntity::ok);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }


    @DeleteMapping("/delete/{clientId}")
    private Mono<ResponseEntity<Void>> deleteClientResources(@PathVariable String clientId){
        try {
            return clientResourceDB.findByClientId(clientId)
                    .flatMap(existing -> clientResourceDB.deleteById(existing.getId())
                            .then(clientResourceCache.deleteByClientId(clientId))
                            .map(ResponseEntity::ok));
        }
        catch (Exception e){
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
    }



}