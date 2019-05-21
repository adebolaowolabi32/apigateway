package com.interswitch.apigateway.route;

import com.interswitch.apigateway.TestConfig;
import com.interswitch.apigateway.config.RouteConfig;
import com.interswitch.apigateway.repository.AbstractMongoRepositoryTests;
import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.HostRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Import({RouteConfig.class, TestConfig.class})
@ActiveProfiles("dev")
@DataMongoTest
public class MongoRouteDefinitionTests extends AbstractMongoRepositoryTests {

    @MockBean
    private BusProperties busProperties;

    @MockBean
    private Validator validator;

    @Autowired
    private MongoRouteDefinitionRepository repository;

    @Autowired
    private ReactiveMongoRouteDefinitionRepository reactiveMongo;

    @Autowired
    private ConversionService conversionService;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        List<RoutePredicateFactory> routePredicateFactories = Arrays.asList(new HostRoutePredicateFactory(),new PathRoutePredicateFactory());
        List<GatewayFilterFactory> gatewayFilterFactories = Arrays.asList(new AddRequestHeaderGatewayFilterFactory());
        MongoRouteDefinitionRepository repository = new MongoRouteDefinitionRepository(reactiveMongo, gatewayFilterFactories,routePredicateFactories,conversionService);

        RouteDefinition definition = new RouteDefinition();
        definition.setId("testapi");
        definition.setUri(new URI("http://httpbin.org:80"));
        List<FilterDefinition> filters = List.of(new FilterDefinition("AddRequestHeader=X-Request-ApiFoo, ApiBaz"));
        List<PredicateDefinition> predicates = List.of(
                new PredicateDefinition("Host=**.apiaddrequestheader.org"),
                new PredicateDefinition("Path=/headers"));
        definition.setFilters(filters);
        definition.setPredicates(predicates);

        RouteDefinition definition2 = new RouteDefinition();
        definition2.setId("testapi2");
        definition2.setUri(new URI("http://httpbin.org:80"));
        List<FilterDefinition> filters2 = List.of(new FilterDefinition("AddRequestHeader=X-Request-ApiFoo, ApiBaz"));
        List<PredicateDefinition> predicates2 = List.of(
                new PredicateDefinition("Host=**.apiaddrequestheader.org"),
                new PredicateDefinition("Path=/headers"));
        definition2.setFilters(filters2);
        definition2.setPredicates(predicates2);

        repository.save(Mono.just(definition)).block();
        repository.save(Mono.just(definition2)).block();

    }

    @Test
    public void testGetRouteDefinitions() {
        StepVerifier.create(repository.getRouteDefinitions().doOnNext(System.out::println)).expectNextCount(2);
    }
}