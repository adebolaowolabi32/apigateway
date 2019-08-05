package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.RouteConfig;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoRouteConfigRepository extends ReactiveMongoRepository<RouteConfig, String> {
    Mono<RouteConfig> findByRouteId(String routeId);
}
