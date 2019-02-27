package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ReactiveMongoClientResources;
import org.assertj.core.api.Assertions;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.when;

@ActiveProfiles("dev")
@WebFluxTest(value = {ClientResourcesController.class}, excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveManagementWebSecurityAutoConfiguration.class,
        ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {ReactiveMongoClientResources.class, ClientResourcesController.class})
public class ClientResourcesControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ReactiveMongoClientResources mongo;

    @Test
    public void testGetClientResources(){
        List testresourceIds = new ArrayList();
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        ClientResources resource = new ClientResources("id","testclientid",testresourceIds);
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
        List testresourceIds = new ArrayList();
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        ClientResources resource = new ClientResources("id","testclientid",testresourceIds);
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
        List testresourceIds = new ArrayList();
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        ClientResources resource = new ClientResources("id","testclientid",testresourceIds);
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
        List testresourceIds = new ArrayList();
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        ClientResources resource = new ClientResources("id","testclientid",testresourceIds);
        when(this.mongo.findById(resource.getId())).thenReturn(Mono.just(resource));
        when(this. mongo.save(resource)).thenReturn(Mono.empty());
        this.webClient.post()
                .uri("/resources/update", resource.getId())
                .body(BodyInserters.fromObject(resource))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(ClientResources.class);
    }
    @Test
    public void testDeletelientResources(){
        List testresourceIds = new ArrayList();
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        ClientResources resource = new ClientResources("id","testclientid",testresourceIds);
        when(mongo.delete(resource))
                .thenReturn(Mono.empty());
        this.webClient.delete()
                .uri("/resources/delete/{id}",  Collections.singletonMap("id","5c769fe30b6ea90d607dc44c"))
                .exchange()
                .expectStatus().isOk();
    }
}
