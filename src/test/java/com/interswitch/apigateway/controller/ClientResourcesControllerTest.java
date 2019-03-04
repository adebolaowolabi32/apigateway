package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.when;

@ActiveProfiles("dev")
@WebFluxTest(value = {ClientResourcesController.class}, excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveManagementWebSecurityAutoConfiguration.class,
        ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {MongoClientResourcesRepository.class, ClientResourcesController.class})
public class ClientResourcesControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoClientResourcesRepository mongo;


    private List testresourceIds = new ArrayList();
    private ClientResources resource = new ClientResources();

    @BeforeEach
    public void setup() throws URISyntaxException {
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        resource = new ClientResources("id","testclientid",testresourceIds);
    }
    @Test
    public void testGetClientResources(){
        when(mongo.findAll()).thenReturn(Flux.just(resource));
        this.webClient.get()
                .uri("/resources")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(ClientResources.class);
    }

    @Test
    public void testSaveClientResources(){
        when(mongo.save(resource)).thenReturn(Mono.just(resource));
        this.webClient.post()
                .uri("/resources/save")
                .body(BodyInserters.fromObject(resource))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(ClientResources.class);
    }

    @Test
    public void findByClientId(){
        when(mongo.findByClientId(resource.getClientId())).thenReturn(Mono.just(resource));
        this.webClient.get()
                .uri("/resources/{clientId}", Collections.singletonMap("clientId",resource.getClientId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> Assertions.assertThat(response.getResponseBody()).isNotNull());
    }

    @Test
    public void testUpdateClientResources(){
        when(this.mongo.findByClientId(resource.getClientId())).thenReturn(Mono.just(resource));
        when(this. mongo.save(resource)).thenReturn(Mono.just(resource));
        this.webClient.put()
                .uri("/resources/update")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(resource))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(ClientResources.class);
    }
    @Test
    public void testDeletelientResources(){
        when(mongo.deleteById(resource.getId())).thenReturn(Mono.empty());
        when(mongo.findById(resource.getId())).thenReturn(Mono.just(resource));
        this.webClient.delete()
                .uri("/resources/delete/{id}",  Collections.singletonMap("id",resource.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}
