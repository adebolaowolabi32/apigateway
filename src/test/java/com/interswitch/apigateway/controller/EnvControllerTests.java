package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.repository.MongoEnvRepository;
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
@ContextConfiguration(classes = {EnvController.class})
public class EnvControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoEnvRepository repository;

    private Env env;

    @BeforeEach
    public void setup() {
        env = new Env();
        env.setId("testRoute");
        env.setRouteId("testRoute");
        env.setUat("https://twitter.com");
        env.setSandbox("https://google.com");
    }

    @Test
    public void testFindAll() {
        when(repository.findAll()).thenReturn(Flux.fromIterable(Collections.singletonList(env)));
        this.webClient.get()
                .uri("/env")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Env.class);
    }

    @Test
    public void testSave() {
        when(repository.findByRouteId(env.getRouteId())).thenReturn(Mono.empty());
        when(repository.save(env)).thenReturn(Mono.just(env));
        this.webClient.post()
                .uri("/env")
                .body(BodyInserters.fromObject(env))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Env.class);
    }

    @Test
    public void testFindByRouteId() {
        when(repository.findByRouteId(env.getRouteId())).thenReturn(Mono.just(env));
        this.webClient.get()
                .uri("/env/{routeId}", env.getRouteId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Env.class);
    }

    @Test
    public void testUpdate() {
        when(repository.findByRouteId(env.getRouteId())).thenReturn(Mono.just(env));
        when(repository.save(env)).thenReturn(Mono.just(env));
        this.webClient.put()
                .uri("/env")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(env))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Env.class);
    }

    @Test
    public void testDelete() {
        when(repository.deleteById(env.getId())).thenReturn(Mono.empty());
        when(repository.findByRouteId(env.getRouteId())).thenReturn(Mono.just(env));
        this.webClient.delete()
                .uri("/env/{routeId}", env.getRouteId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

}