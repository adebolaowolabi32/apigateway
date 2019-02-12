package com.interswitch.apigateway.route;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MongoRouteDefinitionRepository implements RouteDefinitionRepository {

    private ReactiveMongoRouteDefinitionRepository mongo;

    public MongoRouteDefinitionRepository(ReactiveMongoRouteDefinitionRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return mongo.findAll();
    }

    @Override
    public Mono<Void> save(@Validated Mono<RouteDefinition> route) {

        return route.flatMap(r -> mongo.save(r).then());
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return mongo.deleteById(routeId);
    }
}
