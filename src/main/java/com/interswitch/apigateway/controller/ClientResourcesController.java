package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ClientResourcesRepository;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
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
    private Mono<ResponseEntity<ClientResources>> saveClientResource(@Validated @RequestBody ClientResources clientResource){
        return clientResourceDB.findByClientId(clientResource.getClientId())
                .flatMap(existing -> Mono.error(new RuntimeException("Client Resource already exists")))
                .switchIfEmpty(clientResourceDB.save(clientResource).then(clientResourceCache.save(clientResource)))
                .then(Mono.defer(() -> Mono.just(ResponseEntity.created(URI.create("/routes/"+clientResource.getClientId())).build())));
    }
    @GetMapping(value= "/{clientId}", produces = "application/json")
    private Mono<ResponseEntity<ClientResources>> findByClientId(@Validated @PathVariable String clientId){
        return clientResourceCache.findByClientId(clientId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @PutMapping(value="/update",produces = "application/json")
        private Mono<ResponseEntity<ClientResources>> updateClientResources(@Validated @RequestBody ClientResources clientResource) {
        return clientResourceDB.findByClientId(clientResource.getClientId())
                .flatMap(existing -> {
                    existing.setResourceIds(clientResource.getResourceIds());
                    return clientResourceDB.save(existing)
                            .then(clientResourceCache.update(clientResource))
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
