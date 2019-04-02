package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Client;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class ClientCacheRepository {
    private final Map<String, Client> clients = Collections.synchronizedMap(new LinkedHashMap<>());

    public ClientCacheRepository() {
    }

    public Mono<Client> findByClientId(Mono<String> clientId) {
        return clientId.map(this.clients::get);
    }

    public Flux<Client> findAll() {
        return Flux.fromIterable(this.clients.values());
    }

    public Mono<Client> save(Mono<Client> client) {
        return client.flatMap((c) -> {
            this.clients.put(c.getClientId(), c);
            return Mono.empty();
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