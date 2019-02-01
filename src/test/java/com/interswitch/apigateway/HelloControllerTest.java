package com.interswitch.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(HelloController.class)
public class HelloControllerTest {
    @Autowired
    private WebTestClient webClient;

    @Test
    public void testGreetingDefault() {
        this.webClient.get().uri("/greeting")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.content").isEqualTo("Hello, World!");
    }

    @Test
    public void testGreetingQueryParam() {
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/greeting")
                        .queryParam("name", "Alex")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.content").isEqualTo("Hello, Alex!");
    }
}
