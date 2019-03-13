package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import com.interswitch.apigateway.route.MongoRouteDefinitionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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
            buildPassportRoute(repository, "passport-oauth");
        };
    }

    private void buildPassportRoute(MongoRouteDefinitionRepository repository, String id) {
        RouteDefinition routeDefinition = new RouteDefinition();
        List<PredicateDefinition> predicates = new ArrayList<>();
        PredicateDefinition predicateDefinition = new PredicateDefinition("Path=/passport/oauth/**");
        predicates.add(predicateDefinition);
        routeDefinition.setId(id);
        routeDefinition.setUri(URI.create(baseUrl));
        routeDefinition.setOrder(0);
        routeDefinition.setPredicates(predicates);
        repository.save(Mono.just(routeDefinition)).subscribe();
    }


}
