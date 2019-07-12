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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {AccessLogsController.class})
public class AccessLogsControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    MongoAccessLogsRepository mongoAccessLogsRepository;

    private AccessLogs accessLogs;

    @BeforeEach
    public void setup() {
        accessLogs = new AccessLogs();
        accessLogs.setId("accessLogs1");
        accessLogs.setAction(AccessLogs.Action.CREATION);
        accessLogs.setEntity(AccessLogs.Entity.PRODUCT);
        accessLogs.setEntityId("productId");
        accessLogs.setApi("/products");
        accessLogs.setTimestamp(LocalDateTime.now());
        accessLogs.setUsername("user.name");
        accessLogs.setStatus("");
        accessLogs.setState(AccessLogs.State.SUCCESSFUL);    }

    @Test
    public void testGetAll(){
        when(mongoAccessLogsRepository.findAll()).thenReturn(Flux.fromIterable(Collections.singletonList(accessLogs)));

        this.webClient.get()
                .uri("/audit")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(AccessLogs.class);

    }
}
