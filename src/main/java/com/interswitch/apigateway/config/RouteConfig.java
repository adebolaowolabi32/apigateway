package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import com.interswitch.apigateway.route.MongoRouteDefinitionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public MongoRouteDefinitionRepository mongoRouteDefinitionRepository(ReactiveMongoRouteDefinitionRepository mongo) {
        return new MongoRouteDefinitionRepository(mongo);
    }

    @Bean
    public RouteLocator passportRouteLocator(RouteLocatorBuilder builder, @Value("${passport.baseurl}") String baseUrl) {
        RouteLocatorBuilder.Builder routeLocator = builder.routes();
        routeLocator
                .route("passport-authorize", r -> r
                        .host("*")
                        .and()
                        .path("/oauth/authorize")
                        .filters(f -> f.prefixPath("/passport"))
                        .uri(baseUrl))
                .route("passport-token", r -> r
                        .host("*")
                        .and()
                        .path("/oauth/token")
                        .filters(f -> f.prefixPath("/passport").preserveHostHeader())
                        .uri(baseUrl));
        return routeLocator.build();
    }
}
