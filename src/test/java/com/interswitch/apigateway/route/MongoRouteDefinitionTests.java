package com.interswitch.apigateway.route;

import com.interswitch.apigateway.config.RouteConfig;
import com.interswitch.apigateway.repository.AbstractMongoRepositoryTests;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Import(RouteConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MongoRouteDefinitionTests extends AbstractMongoRepositoryTests {
    @Autowired
    private MongoRouteDefinitionRepository repository;

    @BeforeAll
    public void setUp() throws URISyntaxException {
        RouteDefinition definition = new RouteDefinition();
        definition.setId("testapi");
        definition.setUri(new URI("http://httpbin.org:80"));
        List<FilterDefinition> filters = List.of(new FilterDefinition("AddRequestHeader=X-Request-ApiFoo, ApiBaz"));
        List<PredicateDefinition> predicates = List.of(
                new PredicateDefinition("Host=**.apiaddrequestheader.org"),
                new PredicateDefinition("Path=/headers")
        );
        definition.setFilters(filters);
        definition.setPredicates(predicates);

        RouteDefinition definition2 = new RouteDefinition();
        definition2.setId("testapi2");
        definition2.setUri(new URI("http://httpbin.org:80"));
        List<FilterDefinition> filters2 = List.of(new FilterDefinition("AddRequestHeader=X-Request-ApiFoo, ApiBaz"));
        List<PredicateDefinition> predicates2 = List.of(
                new PredicateDefinition("Host=**.apiaddrequestheader.org"),
                new PredicateDefinition("Path=/headers")
        );
        definition2.setFilters(filters2);
        definition2.setPredicates(predicates2);

        repository.save(Mono.just(definition)).block();
        repository.save(Mono.just(definition2)).block();

    }

    @Test
    public void testGetRouteDefinitions() {
        StepVerifier.create(repository.getRouteDefinitions()).expectNextCount(2).verifyComplete();
    }
}
