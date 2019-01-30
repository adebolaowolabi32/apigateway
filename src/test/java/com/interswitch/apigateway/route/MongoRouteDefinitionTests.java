package com.interswitch.apigateway.route;

import com.interswitch.apigateway.config.RouteConfig;
import com.interswitch.apigateway.repository.AbstractMongoRepositoryTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Import(RouteConfig.class)
public class MongoRouteDefinitionTests extends AbstractMongoRepositoryTests {
    @Autowired
    ReactiveMongoOperations operations;
    @Autowired
    private MongoRouteDefinitionRepository repository;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        operations.collectionExists(RouteDefinition.class)
                .flatMap(exists -> exists ? operations.dropCollection(RouteDefinition.class) : Mono.just(exists))
                .flatMap(o -> operations.createCollection(RouteDefinition.class, CollectionOptions.empty().maxDocuments(100).size(1024 * 1024)))
                .then()
                .block();

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
        StepVerifier.create(repository.getRouteDefinitions().doOnNext(System.out::println)).expectNextCount(2).verifyComplete();
    }
}
