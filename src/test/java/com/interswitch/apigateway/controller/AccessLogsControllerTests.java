package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

@ActiveProfiles("dev")
@EnableAutoConfiguration
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class, MongoAccessLogsRepository.class})
public class AccessLogsControllerTests {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    MongoAccessLogsRepository mongoAccessLogsRepository;

    @Autowired
    AccessLogsController accessLogsController;
    private AccessLogs accessLogs;

    @BeforeEach
    public void setup() {
        accessLogs = new AccessLogs();
        accessLogs.setId("accessLogs1");
        accessLogs.setAction(AccessLogs.Action.CREATE);
        accessLogs.setEntity(AccessLogs.Entity.PRODUCT);
        accessLogs.setEntityId("productId");
        accessLogs.setApi("/products");
        accessLogs.setTimestamp(LocalDateTime.now());
        accessLogs.setUsername("user.name");
        accessLogs.setStatus(AccessLogs.Status.SUCCESSFUL);
    }

    @Test
    public void testGetPagedDefaultValues(){
        this.webClient.get()
                .uri("/audit")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(AccessLogs.class).contains(accessLogs);
    }

    @Test
    public void testGetPaged(){
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit")
                        .queryParam("pageNum", "40")
                        .queryParam("pageSize", "20")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(AccessLogs.class).contains(accessLogs);
    }

    @Test
    public void testGetSearchValue(){
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit/search")
                        .queryParam("pageNum", "0")
                        .queryParam("pageSize", "30")
                        .queryParam("searchValue", "adebola.owolabi")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(AccessLogs.class).contains(accessLogs);
    }
}
