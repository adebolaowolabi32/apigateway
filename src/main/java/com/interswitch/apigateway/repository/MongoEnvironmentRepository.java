package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Environment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoEnvironmentRepository extends ReactiveMongoRepository<Environment, String> {
    Mono<Environment> findByRouteId(String routeId);
}
