package com.interswitch.apigateway.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@ActiveProfiles("dev")
@DataMongoTest
public class RouteDefinitionRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    private ReactiveMongoRouteDefinitionRepository repository;

    @Test
    public void testFindRouteDefinition() throws URISyntaxException {
        var definition = new RouteDefinition();
        definition.setId("testapi");
        definition.setUri(new URI("http://httpbin.org:80"));
        var filters = List.of(new FilterDefinition("AddRequestHeader=X-Request-ApiFoo, ApiBaz"));
        var predicates = List.of(
                new PredicateDefinition("Host=**.apiaddrequestheader.org"),
                new PredicateDefinition("Path=/headers")
        );

        definition.setFilters(filters);
        definition.setPredicates(predicates);

        repository.save(definition).block();

        Mono<RouteDefinition> definitionMono = repository.findById("testapi");

        StepVerifier.create(definitionMono).assertNext(r -> {
            assertThat(r.getId()).isEqualTo("testapi");
            assertThat(r.getFilters()).hasSize(1);
            assertThat(r.getPredicates()).hasSize(2);
        })
                .expectComplete()
                .verify();
    }

    @Test
    public void testSaveRouteDefinition() throws URISyntaxException {
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
        StepVerifier.create(repository.save(definition)).expectNext(definition).expectComplete().verify();
    }
}
