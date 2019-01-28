package com.interswitch.apigateway.service;

import com.interswitch.apigateway.repository.RouteDefinitionRepositoryMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.actuate.GatewayControllerEndpoint;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ValidationException;
import java.net.URI;
import java.util.List;

@Component
public class RouteRepositoryService implements RouteDefinitionRepository {

    @Autowired
    private RouteDefinitionRepositoryMongo routeDefinitionRepositoryMongo;

    @Autowired
    private GatewayControllerEndpoint gatewayControllerEndpoint;

    @Override
    @Validated
    public Mono<Void> save(Mono<RouteDefinition> route ) {
        return route.flatMap(r -> {
            routeDefinitionRepositoryMongo.save(r).subscribe();
            gatewayControllerEndpoint.refresh();
            return Mono.empty();

        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            routeDefinitionRepositoryMongo.deleteById(id).subscribe();
            gatewayControllerEndpoint.refresh();
            return Mono.empty();
        });
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return routeDefinitionRepositoryMongo.findAll();
    }

}

