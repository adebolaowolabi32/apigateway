package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.MongoClientResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/resources")
public class ClientResourcesController {

    private MongoClientResources clientResourceDB;

    public ClientResourcesController(MongoClientResources clientResourceDB) {
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
    private Mono<ResponseEntity<ClientResources>> findByClientId(@Validated @PathVariable String clientId){
        return clientResourceDB.findByClientId(clientId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @PutMapping(value="/update/{id}", produces = "application/json")
    private Mono<ResponseEntity<ClientResources>> updateClientResources(@PathVariable(value = "id") String Id, @Validated @RequestBody ClientResources clientResource) {
        return clientResourceDB.findById(Id.toLowerCase())
                .map(existingData -> {
                    existingData.setId(Id);
                    existingData.setClientId(clientResource.getClientId());
                    existingData.setResourceIds(clientResource.getResourceIds());
                    clientResourceDB.save(existingData).subscribe();
                    return ResponseEntity.status(HttpStatus.OK).body(existingData);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }


    @DeleteMapping("/delete/{id}")
    private Mono<ResponseEntity<Void>> deleteClientResources(@PathVariable String id){
        try {
            return clientResourceDB.deleteById(id)
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
        }
        catch (Exception e){
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

    }



}
