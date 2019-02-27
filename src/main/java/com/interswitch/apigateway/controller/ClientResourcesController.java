package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ReactiveMongoClientResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/resources")
public class ClientResourcesController {

    private ReactiveMongoClientResources clientResourceDB;

    public ClientResourcesController(ReactiveMongoClientResources clientResourceDB) {
        this.clientResourceDB = clientResourceDB;
    }

    @GetMapping(produces = "application/json")
    private Flux<ClientResources> getAllClientResources() {
        return clientResourceDB.findAll();
    }

    @PostMapping (value = "/save", produces = "application/json")
    private Mono<ClientResources> saveClientResource(@Validated @RequestBody ClientResources clientResource){
        return clientResourceDB.save(clientResource);
    }

    @GetMapping(value= "/{clientId}", produces = "application/json")
    private Mono<ClientResources> findByClientId(@Validated @PathVariable String clientId){
        return clientResourceDB.findByClientId(clientId);

    }

    @PostMapping(value="/update/{id}", produces = "application/json")
    private Mono<ResponseEntity<ClientResources>> updateClientResources(@PathVariable(value = "id") String Id, @Validated @RequestBody ClientResources clientResource) {
        return clientResourceDB.findById(Id.toLowerCase())
                .map(existingData -> {
                    existingData.setId(Id);
                    existingData.setClientId(clientResource.getClientId());
                    existingData.setResourceIds(clientResource.getResourceIds());
                    Mono<ClientResources> updated = clientResourceDB.save(existingData);
                    return ResponseEntity.status(HttpStatus.OK).body(updated.block());
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }


    @RequestMapping(value = "/delete/{id}", produces = "application/json", method = RequestMethod.DELETE)
    private Mono<Void> deleteClientResources(@Validated @PathVariable String id){

        return clientResourceDB.deleteById(id);


    }

}
