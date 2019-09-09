package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.RouteEnvironment;
import com.interswitch.apigateway.repository.MongoRouteEnvironmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {RouteEnvironmentController.class})
public class RouteEnvironmentControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoRouteEnvironmentRepository repository;

    private RouteEnvironment routeEnvironment;

    @BeforeEach
    public void setup() {
        routeEnvironment = new RouteEnvironment();
        routeEnvironment.setId("testRoute");
        routeEnvironment.setRouteId("testRoute");
        routeEnvironment.setTestURL("https://twitter.com");
    }

    @Test
    public void testFindAll() {
        when(repository.findAll()).thenReturn(Flux.fromIterable(Collections.singletonList(routeEnvironment)));
        this.webClient.get()
                .uri("/env")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(RouteEnvironment.class);
    }

    @Test
    public void testSave() {
        when(repository.existsByRouteId(routeEnvironment.getRouteId())).thenReturn(Mono.just(false));
        when(repository.save(routeEnvironment)).thenReturn(Mono.just(routeEnvironment));
        this.webClient.post()
                .uri("/env")
                .body(BodyInserters.fromObject(routeEnvironment))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RouteEnvironment.class);
    }

    @Test
    public void testFindByRouteId() {
        when(repository.findByRouteId(routeEnvironment.getRouteId())).thenReturn(Mono.just(routeEnvironment));
        this.webClient.get()
                .uri("/env/{routeId}", routeEnvironment.getRouteId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(RouteEnvironment.class);
    }

    @Test
    public void testUpdate() {
        when(repository.findByRouteId(routeEnvironment.getRouteId())).thenReturn(Mono.just(routeEnvironment));
        when(repository.save(routeEnvironment)).thenReturn(Mono.just(routeEnvironment));
        this.webClient.put()
                .uri("/env")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(routeEnvironment))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(RouteEnvironment.class);
    }

    @Test
    public void testDelete() {
        when(repository.deleteById(routeEnvironment.getId())).thenReturn(Mono.empty());
        when(repository.findByRouteId(routeEnvironment.getRouteId())).thenReturn(Mono.just(routeEnvironment));
        this.webClient.delete()
                .uri("/env/{routeId}", routeEnvironment.getRouteId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

}