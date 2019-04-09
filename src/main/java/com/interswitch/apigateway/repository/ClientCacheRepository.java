package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Client;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.synchronizedMap;


public class ClientCacheRepository {
    private final Map<String, Client> clients = synchronizedMap(new LinkedHashMap<>());

    private Mono<List<Client>> clientList;
    public ClientCacheRepository() {
    }

    public ClientCacheRepository(ClientMongoRepository clientMongoRepository){
           clientList = clientMongoRepository.findAll().collectList();
        clientMongoRepository.findAll().collectList().flatMap(client -> {
            for (var c :client) {
                this.clients.put(c.getClientId(), c);
            }
            return Mono.empty();
        });
    }

    private Mono<Void> loadClients(){
        return clientMongoRepository.findAll().collectList().flatMap(client -> {
            for (var c :client) {
               this.clients.put(c.getClientId(), c);
            }
            return Mono.empty();
        });
    }

    public Mono<Client> findByClientId(Mono<String> clientId) {
        return clientId.flatMap(key -> {
            if (this.clients.containsKey(key)) {
                return Mono.just(this.clients.get(key));
            }
            return Mono.empty();
        });
    }

    public Flux<Client> findAll() {
        return Flux.fromIterable(this.clients.values());
    }

    public Mono<Client> save(Mono<Client> client) {
        return client.flatMap((c) -> {
            this.clients.put(c.getClientId(), c);
            return client;
        });
    }

    public Mono<Void> deleteByClientId(Mono<String> clientId) {
        return clientId.flatMap((cId) -> {
            if (this.clients.containsKey(cId)) {
                this.clients.remove(cId);
                return Mono.empty();
            } else {
                return Mono.defer(() -> Mono.error(new NotFoundException("Client Permissions not found for ClientID: " + cId)));
            }
        });
    }

}