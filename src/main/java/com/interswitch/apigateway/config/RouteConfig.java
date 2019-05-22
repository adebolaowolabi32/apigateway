package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import com.interswitch.apigateway.route.MongoRouteDefinitionRepository;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RouteConfig{
    @Bean
    public MongoRouteDefinitionRepository mongoRouteDefinitionRepository(ReactiveMongoRouteDefinitionRepository mongo, List<GatewayFilterFactory> gatewayFilterFactories, List<RoutePredicateFactory> predicates) {
        return new MongoRouteDefinitionRepository(mongo, gatewayFilterFactories, predicates);
    }

}
