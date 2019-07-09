package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.repository.MongoClientRepository;
import com.interswitch.apigateway.repository.MongoProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private MongoClientRepository mongoClientRepository;
    private MongoProductRepository mongoProductRepository;

    public ClientController(MongoClientRepository mongoClientRepository, MongoProductRepository mongoProductRepository) {
        this.mongoClientRepository = mongoClientRepository;
        this.mongoProductRepository = mongoProductRepository;
    }

    @GetMapping(produces = "application/json")
    private Flux<Client> getAll() {
        return mongoClientRepository.findAll();
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Client> save(@Validated @RequestBody Client client){
        client.setProducts(new ArrayList<>());
        return mongoClientRepository.save(client).onErrorMap(throwable -> {
            return new ResponseStatusException(HttpStatus.CONFLICT,"Client already exists");
        });
    }

    @GetMapping(value= "/{clientId}", produces = "application/json")
    private Mono<ResponseEntity<Client>> findByClientId(@PathVariable String clientId){
        return mongoClientRepository.findByClientId(clientId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{clientId}")
    private Mono<ResponseEntity<Void>> delete(@PathVariable String clientId){
        try {
            return mongoClientRepository.deleteById(clientId)
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
        }
        catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
    }

    @GetMapping(value= "/{clientId}/products", produces = "application/json")
    private Mono<List<Product>> GetAssignedProducts(@PathVariable String clientId){
        return mongoClientRepository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Client does not exist")))
                .map(client -> client.getProducts());
    }

    @PostMapping(value= "/{clientId}/products/{productId}", produces = "application/json", consumes = "application/json")
    private Mono<Client> assignProduct(@PathVariable String clientId, @PathVariable String productId){
        return mongoClientRepository.findByClientId(clientId).flatMap(client ->
                mongoProductRepository.findById(productId).flatMap(product -> {
                    if(!product.getClients().contains(client)) {
                        product.addClient(client);
                    }
                    return mongoProductRepository.save(product).flatMap(p-> {
                        if(!client.getProducts().contains(product)){
                            client.addProduct(product);
                            return mongoClientRepository.save(client);
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Product already assigned to client"));
                    });
                }).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Product does not exist")))
        ).switchIfEmpty(Mono.error(new RuntimeException("Client does not exist")));
    }

    @DeleteMapping(value= "/{clientId}/products/{productId}", produces = "application/json")
    private Mono<Client> unassignProduct(@PathVariable String clientId, @PathVariable String productId){
        return mongoClientRepository.findByClientId(clientId).flatMap(client ->
                mongoProductRepository.findById(productId).flatMap(product -> {
                    if(product.getClients().contains(client)) {
                        product.removeClient(client);
                    }
                    return mongoProductRepository.save(product).flatMap(p->{
                        if(client.getProducts().contains(product)) {
                            client.removeProduct(product);
                            return mongoClientRepository.save(client);
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Product is not assigned to Client"));
                    });
                }).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Product does not exist")))
        ).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Client does not exist")));
    }
}
