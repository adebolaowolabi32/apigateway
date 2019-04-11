package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.refresh.AutoBusRefresh;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private ClientMongoRepository clientMongoRepository;
    private ClientCacheRepository clientCacheRepository;
    private AutoBusRefresh autoBusRefresh;

    public ClientController(ClientMongoRepository clientMongoRepository, ClientCacheRepository clientCacheRepository, AutoBusRefresh autoBusRefresh) {
        this.clientMongoRepository = clientMongoRepository;
        this.clientCacheRepository = clientCacheRepository;
        this.autoBusRefresh = autoBusRefresh;
    }

    @GetMapping(produces = "application/json")
    private Flux<Client> getAllClients() {
        return clientCacheRepository.findAll();
    }

    @PostMapping (value = "/save", produces = "application/json")
    private Mono<ResponseEntity<Client>> saveClient(@Validated @RequestBody Client client){
        return clientMongoRepository.findByClientId(client.getClientId())
                .flatMap(existing -> Mono.error(new RuntimeException("Client Permissions already exists")))
                .switchIfEmpty(clientMongoRepository.save(client).then(clientCacheRepository.save(Mono.just(client))))
                .map(saved -> {
                    autoBusRefresh.publishRefreshEvent();
                    return ResponseEntity.status(HttpStatus.CREATED).body(client);
                });
    }

    @GetMapping(value= "/{clientId}", produces = "application/json")
    private Mono<ResponseEntity<Client>> findClientByClientId(@Validated @PathVariable String clientId){
        return clientCacheRepository.findByClientId(Mono.just(clientId))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @PutMapping(value="/update",produces = "application/json")
    private Mono<ResponseEntity<Client>> updateClient(@Validated @RequestBody Client client) {
        return clientMongoRepository.findByClientId(client.getClientId())
                .flatMap(existing -> {
                    client.setId(existing.getId());
                    return clientMongoRepository.save(client).then(clientCacheRepository.save(Mono.just(client))).map(saved -> {
                        autoBusRefresh.publishRefreshEvent();
                        return ResponseEntity.status(HttpStatus.OK).body(client);
                    });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/delete/{clientId}")
    private Mono<ResponseEntity<Void>> deleteClient(@PathVariable String clientId){
        try {
            return clientMongoRepository.findByClientId(clientId)
                    .flatMap(existing -> clientMongoRepository.deleteById(existing.getId())
                            .then(clientCacheRepository.deleteByClientId(Mono.just(clientId)))
                            .then(Mono.defer(() -> {
                                autoBusRefresh.publishRefreshEvent();
                                return Mono.just(ResponseEntity.status(HttpStatus.OK).build());
                            })));
        }
        catch (Exception e){
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
    }
}
