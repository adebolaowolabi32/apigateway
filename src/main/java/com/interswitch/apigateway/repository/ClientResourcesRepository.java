package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.ClientResources;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Mono;

public class ClientResourcesRepository{
    ReactiveRedisOperations<String, ClientResources> template;

    private String CLIENT_RESOURCE_KEY = "ClientResources";

    public ClientResourcesRepository(ReactiveRedisOperations<String, ClientResources> template) {
        this.template = template;
    }

    public Mono<ClientResources> findByClientId(String id) {
        return template.<String, ClientResources>opsForHash().get(CLIENT_RESOURCE_KEY, id);
    }

    public Mono<ClientResources> save(ClientResources clientResources) {
        return template.opsForHash().put(CLIENT_RESOURCE_KEY, clientResources.getClientId(), clientResources)
                .map(s -> clientResources).log();
    }

    public Mono<ClientResources> update(ClientResources clientResources) {
        return findByClientId(clientResources.getClientId())
                .switchIfEmpty(Mono.error(new RuntimeException("Client ID not found")))
                .flatMap(existingSession -> save(clientResources).log());
    }

    public Mono<Void> deleteByClientId(String id) {
        return template.opsForHash().remove(CLIENT_RESOURCE_KEY, id)
                .flatMap(p -> Mono.<Void>empty()).log();
    }

}