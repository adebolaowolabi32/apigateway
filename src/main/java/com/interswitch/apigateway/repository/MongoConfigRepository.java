package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Config;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoConfigRepository extends ReactiveMongoRepository<Config, String> {
    Mono<Config> findByRouteId(String routeId);
}
