package com.interswitch.apigateway.controller;


import com.interswitch.apigateway.model.Config;
import com.interswitch.apigateway.repository.MongoConfigRepository;
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

import java.net.URI;
import java.util.Collections;

import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {ConfigController.class})
public class ConfigControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoConfigRepository repository;

    private Config config;

    @BeforeEach
    public void setup() {
        config = new Config();
        config.setId("testRoute");
        config.setRouteId("testRoute");
        config.setUat(URI.create("https://twitter.com"));
        config.setSandbox(URI.create("https://google.com"));
    }

    @Test
    public void testFindAll() {
        when(repository.findAll()).thenReturn(Flux.fromIterable(Collections.singletonList(config)));
        this.webClient.get()
                .uri("/config")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Config.class);
    }

    @Test
    public void testSave() {
        when(repository.findByRouteId(config.getRouteId())).thenReturn(Mono.empty());
        when(repository.save(config)).thenReturn(Mono.just(config));
        this.webClient.post()
                .uri("/config")
                .body(BodyInserters.fromObject(config))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Config.class);
    }

    @Test
    public void testFindByRouteId() {
        when(repository.findByRouteId(config.getRouteId())).thenReturn(Mono.just(config));
        this.webClient.get()
                .uri("/config/{routeId}", config.getRouteId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Config.class);
    }

    @Test
    public void testUpdate() {
        when(repository.findByRouteId(config.getRouteId())).thenReturn(Mono.just(config));
        when(repository.save(config)).thenReturn(Mono.just(config));
        this.webClient.put()
                .uri("/config")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(config))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Config.class);
    }

    @Test
    public void testDelete() {
        when(repository.deleteById(config.getId())).thenReturn(Mono.empty());
        when(repository.findByRouteId(config.getRouteId())).thenReturn(Mono.just(config));
        this.webClient.delete()
                .uri("/config/{routeId}", config.getRouteId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

}
