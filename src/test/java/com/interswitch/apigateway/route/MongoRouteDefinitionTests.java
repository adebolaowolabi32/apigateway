package com.interswitch.apigateway.route;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.HostRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class MongoRouteDefinitionTests {
    @Autowired
    private Validator validator ;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    @Qualifier("webFluxConversionService")
    private ConversionService conversionService;

    @Autowired
    private MongoRouteDefinitionRepository repository;

    @Autowired
    private ReactiveMongoRouteDefinitionRepository reactiveMongo;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        List<RoutePredicateFactory> routePredicateFactories = Arrays.asList(new HostRoutePredicateFactory(),new PathRoutePredicateFactory());
        List<GatewayFilterFactory> gatewayFilterFactories = Arrays.asList(new AddRequestHeaderGatewayFilterFactory());
        repository = new MongoRouteDefinitionRepository(reactiveMongo, gatewayFilterFactories,routePredicateFactories,validator,conversionService,beanFactory);

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

    @Test
    public void testWrongPredicateName() throws URISyntaxException {
        RouteDefinition definition = new RouteDefinition();
        definition.setId("testapi");
        definition.setUri(new URI("http://httpbin.org:80"));
        List<PredicateDefinition> wrongPredicateName = List.of(
                new PredicateDefinition("Hot=**.apiaddrequestheader.org"));
        definition.setPredicates(wrongPredicateName);
        StepVerifier.create(repository.save(Mono.just(definition))).expectError(ResponseStatusException.class).verify();
    }

    @Test
    public void testWrongPredicateArguments() throws URISyntaxException {
        RouteDefinition definition = new RouteDefinition();
        definition.setId("testapi");
        definition.setUri(new URI("http://httpbin.org:80"));
        List<PredicateDefinition> wrongPredicateArgument = List.of(
                new PredicateDefinition("Method=YUFHIF"));
        definition.setPredicates(wrongPredicateArgument);
        StepVerifier.create(repository.save(Mono.just(definition))).expectError(ResponseStatusException.class).verify();
    }
    @Test
    public void testWrongFilterName() throws URISyntaxException {
        RouteDefinition definition = new RouteDefinition();
        definition.setId("testapi");
        definition.setUri(new URI("http://httpbin.org:80"));
        List<FilterDefinition> wrongFilterName = List.of(new FilterDefinition("NoFilter=X-Request-ApiFoo, ApiBaz"));
        List<PredicateDefinition> predicates = List.of(
                new PredicateDefinition("Path=/headers"));
        definition.setFilters(wrongFilterName);
        definition.setPredicates(predicates);
        StepVerifier.create(repository.save(Mono.just(definition))).expectError(ResponseStatusException.class).verify();
    }
    @Test
    public void testWrongFilterArguments() throws URISyntaxException {
        RouteDefinition definition = new RouteDefinition();
        definition.setId("testapi");
        definition.setUri(new URI("http://httpbin.org:80"));
        List<FilterDefinition> wrongFilterArguments = List.of(new FilterDefinition("AddRequestHeader=X-Request-ApiFoo"));
        List<PredicateDefinition> predicates = List.of(
                new PredicateDefinition("Path=/headers"));
        definition.setFilters(wrongFilterArguments);
        definition.setPredicates(predicates);
        StepVerifier.create(repository.save(Mono.just(definition))).expectError(ResponseStatusException.class).verify();
    }

}