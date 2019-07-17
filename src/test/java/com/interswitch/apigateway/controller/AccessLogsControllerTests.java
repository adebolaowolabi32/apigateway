package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest( excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = AccessLogsController.class)
public class AccessLogsControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoAccessLogsRepository mongoAccessLogsRepository;

    private AccessLogs accessLogs;

    @BeforeEach
    public void setup(){
        accessLogs = new AccessLogs();
        accessLogs.setId("accessLogs1");
        accessLogs.setAction(AccessLogs.Action.CREATE);
        accessLogs.setEntity(AccessLogs.Entity.PRODUCT);
        accessLogs.setEntityId("productId");
        accessLogs.setApi("/products");
        accessLogs.setTimestamp(LocalDateTime.now());
        accessLogs.setClient("client_id");
        accessLogs.setUsername("user.name");
        accessLogs.setStatus(AccessLogs.Status.SUCCESSFUL);
    }

    @Test
    public void testGetPagedDefaultValues() {
        when(mongoAccessLogsRepository.retrieveAllPaged(any(PageRequest.class))).thenReturn(Flux.fromIterable(Collections.singleton(accessLogs)));
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
        when(mongoAccessLogsRepository.retrieveAllPaged(any(PageRequest.class))).thenReturn(Flux.fromIterable(Collections.singleton(accessLogs)));
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit")
                        .queryParam("pageNum", "2")
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
        when(mongoAccessLogsRepository.query(any(String.class), any(PageRequest.class))).thenReturn(Flux.fromIterable(Collections.singleton(accessLogs)));
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit/search")
                        .queryParam("pageNum", "1")
                        .queryParam("pageSize", "10")
                        .queryParam("searchValue", "user.name")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(AccessLogs.class).contains(accessLogs);
    }
}
