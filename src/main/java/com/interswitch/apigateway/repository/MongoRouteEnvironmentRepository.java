package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.RouteEnvironment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MongoRouteEnvironmentRepository extends ReactiveMongoRepository<RouteEnvironment, String> {
    Mono<RouteEnvironment> findByRouteId(String routeId);

    Mono<Boolean> existsByRouteId(String routeId);
}
