package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Env;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoEnvRepository extends ReactiveMongoRepository<Env, String> {
    Mono<Env> findByRouteId(String routeId);
}
