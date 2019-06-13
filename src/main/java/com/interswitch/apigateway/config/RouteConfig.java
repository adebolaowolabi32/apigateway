package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import com.interswitch.apigateway.route.MongoRouteDefinitionRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RouteConfig {
    @Value("${passport.baseurl}")
    String baseUrl;

    @Bean
    public MongoRouteDefinitionRepository mongoRouteDefinitionRepository(ReactiveMongoRouteDefinitionRepository mongo, List<GatewayFilterFactory> gatewayFilterFactories, List<RoutePredicateFactory> routePredicateFactories, Validator validator, @Qualifier("webFluxConversionService") ConversionService conversionService, BeanFactory beanFactory) {
        return new MongoRouteDefinitionRepository(mongo, gatewayFilterFactories,routePredicateFactories,validator,conversionService,beanFactory);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MongoRouteDefinitionRepository mongoRouteDefinitionRepository){
        return commandLineRunner -> {
            RouteDefinition routeDefinition = new RouteDefinition();
            List<PredicateDefinition> predicates = new ArrayList<>();
            PredicateDefinition predicateDefinition = new PredicateDefinition("Path=" + "/passport/**");
            predicates.add(predicateDefinition);
            routeDefinition.setId("passport");
            routeDefinition.setUri(URI.create(baseUrl));
            routeDefinition.setOrder(0);
            routeDefinition.setPredicates(predicates);
            mongoRouteDefinitionRepository.save(Mono.just(routeDefinition)).subscribe();
        };
    }
}
