package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Client;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;

@Repository
public class ClientCacheRepository {
    ReactiveRedisOperations<String, Client> template;

    private String CLIENT_KEY = "Client";

    public ClientCacheRepository(ReactiveRedisOperations<String, Client> template) {
        this.template = template;
    }

    public Mono<Client> findByClientId(String clientId) {
        return template.<String, Client>opsForHash().get(CLIENT_KEY, clientId);
    }

    public Flux<Map.Entry<String, Client>> findAll() {
        return template.<String, Client>opsForHash().entries(CLIENT_KEY);
    }

    public Mono<Client> save(Client client) {
        return template.opsForHash().put(CLIENT_KEY, client.getClientId(), client)
                .map(s -> client).log();
    }

    public Mono<Client> update(Client client) {
        return findByClientId(client.getClientId())
                .switchIfEmpty(Mono.error(new RuntimeException("Client ID not found")))
                .flatMap(existingSession -> save(client).log());
    }

    public Mono<Void> deleteByClientId(String clientId) {
        return template.opsForHash().remove(CLIENT_KEY, clientId)
                .flatMap(p -> Mono.<Void>empty()).log();
    }

}