package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.ClientResources;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MongoClientResourcesRepository extends ReactiveMongoRepository<ClientResources, String> {
    Mono<ClientResources> findByClientId(String clientId);
    
}
