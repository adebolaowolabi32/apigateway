package com.interswitch.apigateway.service;

import com.interswitch.apigateway.repository.RouteDefinitionRepositoryMongo;
import com.interswitch.apigateway.route.GatewayRoutesRefresher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
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
    private GatewayRoutesRefresher gatewayRoutesRefresher;

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            URI uri = r.getUri();
            List<PredicateDefinition> predicates = r.getPredicates();
            List<FilterDefinition> filters = r.getFilters();

            if (uri == null)
                throw new ValidationException("URI cannot be null");

            if (predicates == null || predicates.isEmpty())
                throw new ValidationException("Predicates cannot be null or empty");

            if (filters == null)
                throw new ValidationException("Filters cannot be null");

            routeDefinitionRepositoryMongo.save(r).subscribe();
            gatewayRoutesRefresher.refreshRoutes();
            return Mono.empty();

        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            routeDefinitionRepositoryMongo.deleteById(id).subscribe();
            gatewayRoutesRefresher.refreshRoutes();
            return Mono.empty();
        });
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return routeDefinitionRepositoryMongo.findAll();
    }

}

