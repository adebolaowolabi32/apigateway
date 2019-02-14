package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import com.interswitch.apigateway.route.MongoRouteDefinitionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RouteConfig {

    @Value("${passport.baseurl}")
    String baseUrl;

    @Bean
    public MongoRouteDefinitionRepository mongoRouteDefinitionRepository(ReactiveMongoRouteDefinitionRepository mongo) {
        return new MongoRouteDefinitionRepository(mongo);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MongoRouteDefinitionRepository repository){

        return commandLineRunner -> {
            buildPassportRoute(repository, "token", "passport-token");
            buildPassportRoute(repository, "authorize", "passport-authorize");
        };
    }

    private void buildPassportRoute(MongoRouteDefinitionRepository repository, String path, String id) {
        RouteDefinition routeDefinition = new RouteDefinition();
        List<PredicateDefinition> predicates = new ArrayList<>();
        PredicateDefinition predicateDefinition =new PredicateDefinition("Path=/oauth/"+path);
        List<FilterDefinition> filters = new ArrayList<>();
        FilterDefinition filterDefinition = new FilterDefinition("PrefixPath=/passport");
        predicates.add(predicateDefinition);
        filters.add(filterDefinition);
        routeDefinition.setId(id);
        routeDefinition.setUri(URI.create(baseUrl));
        routeDefinition.setOrder(0);
        routeDefinition.setPredicates(predicates);
        routeDefinition.setFilters(filters);
        repository.save(Mono.just(routeDefinition)).subscribe();
    }


}
