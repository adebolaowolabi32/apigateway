package com.interswitch.apigateway.route;

import com.interswitch.apigateway.refresh.AutoBusRefresh;
import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MongoRouteDefinitionRepository implements RouteDefinitionRepository {

    private ReactiveMongoRouteDefinitionRepository mongo;
    private final Map<String, GatewayFilterFactory> gatewayFilterFactories = new HashMap<>();
    private final Map<String, RoutePredicateFactory> routePredicateFactories = new LinkedHashMap<>();

    private AutoBusRefresh autoBusRefresh;

    public MongoRouteDefinitionRepository(ReactiveMongoRouteDefinitionRepository mongo, AutoBusRefresh autoBusRefresh, List<GatewayFilterFactory> gatewayFilterFactories, List<RoutePredicateFactory> routePredicateFactories) {
        this.mongo = mongo;
        this.autoBusRefresh = autoBusRefresh;
        initFactories(gatewayFilterFactories, routePredicateFactories);
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return mongo.findAll();
    }

    @Override
    public Mono<Void> save(@Validated Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            List<PredicateDefinition> predicates = r.getPredicates();
            List<FilterDefinition> filters = r.getFilters();
            if(!checkGatewayPredicatesExist(predicates)){
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gateway Predicate(s) Does Not Exist"));
            }
            if(!filters.isEmpty())
            {
                if(!checkGatewayFiltersExists(filters))
                    {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gateway Filter(s) Does Not Exist"));
                    }
            }
            return mongo.save(r).then(Mono.defer(() -> {
                autoBusRefresh.publishRefreshEvent();
             return Mono.empty();
            }));

        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return mongo.deleteById(routeId).then(Mono.defer(() -> {
            autoBusRefresh.publishRefreshEvent();
            return Mono.empty();
        }));
    }

    private boolean checkGatewayFiltersExists(List<FilterDefinition> filterDefinitions) {
        return filterDefinitions.stream().allMatch(filterDefinition -> {
            GatewayFilterFactory factory = this.gatewayFilterFactories.get(filterDefinition.getName());
            return factory != null;
        });

    }

    private boolean checkGatewayPredicatesExist (List<PredicateDefinition> predicateDefinitions){
        return predicateDefinitions.stream().allMatch(predicateDefinition -> {
            RoutePredicateFactory factory = this.routePredicateFactories.get(predicateDefinition.getName());
            return factory != null;
        });
    }

    private void initFactories(List<GatewayFilterFactory> filters, List<RoutePredicateFactory> predicates) {
        filters.forEach(
                factory -> this.gatewayFilterFactories.put(factory.name(), factory));
        predicates.forEach(
                factory -> this.routePredicateFactories.put(factory.name(), factory));
    }

}
