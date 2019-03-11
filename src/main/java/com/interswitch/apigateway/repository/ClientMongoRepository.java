package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Client;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ClientMongoRepository extends ReactiveMongoRepository<Client, String> {
    Mono<Client> findByClientId(String clientId);
}
