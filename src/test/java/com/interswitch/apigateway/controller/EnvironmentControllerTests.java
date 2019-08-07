package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Environment;
import com.interswitch.apigateway.repository.MongoEnvironmentRepository;
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
@ContextConfiguration(classes = {EnvironmentController.class})
public class EnvironmentControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoEnvironmentRepository repository;

    private Environment environment;

    @BeforeEach
    public void setup() {
        environment = new Environment();
        environment.setId("testRoute");
        environment.setRouteId("testRoute");
        environment.setUat("https://twitter.com");
        environment.setSandbox("https://google.com");
    }

    @Test
    public void testFindAll() {
        when(repository.findAll()).thenReturn(Flux.fromIterable(Collections.singletonList(environment)));
        this.webClient.get()
                .uri("/environment")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Environment.class);
    }

    @Test
    public void testSave() {
        when(repository.findByRouteId(environment.getRouteId())).thenReturn(Mono.empty());
        when(repository.save(environment)).thenReturn(Mono.just(environment));
        this.webClient.post()
                .uri("/environment")
                .body(BodyInserters.fromObject(environment))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Environment.class);
    }

    @Test
    public void testFindByRouteId() {
        when(repository.findByRouteId(environment.getRouteId())).thenReturn(Mono.just(environment));
        this.webClient.get()
                .uri("/environment/{routeId}", environment.getRouteId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Environment.class);
    }

    @Test
    public void testUpdate() {
        when(repository.findByRouteId(environment.getRouteId())).thenReturn(Mono.just(environment));
        when(repository.save(environment)).thenReturn(Mono.just(environment));
        this.webClient.put()
                .uri("/environment")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(environment))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Environment.class);
    }

    @Test
    public void testDelete() {
        when(repository.deleteById(environment.getId())).thenReturn(Mono.empty());
        when(repository.findByRouteId(environment.getRouteId())).thenReturn(Mono.just(environment));
        this.webClient.delete()
                .uri("/environment/{routeId}", environment.getRouteId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

}