package com.interswitch.apigateway.repository;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteDefinitionRepositoryMongo extends ReactiveMongoRepository<RouteDefinition, String> {
}
