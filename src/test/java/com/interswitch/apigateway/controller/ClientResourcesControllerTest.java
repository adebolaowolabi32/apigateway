package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ClientResourcesRepository;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
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
import java.util.*;

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

    @MockBean
    private ClientResourcesRepository cache;


    private List testresourceIds;
    private ClientResources resource;
    private String  clientId = "testclientid";

    @BeforeEach
    public void setup() throws URISyntaxException {
        testresourceIds = new ArrayList();
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        resource = new ClientResources("id",clientId,testresourceIds);
    }
    @Test
    public void testGetAllClientResources(){
        ArrayList<Map.Entry<String, ClientResources>> arr = new ArrayList<>();
        arr.add(new AbstractMap.SimpleEntry(resource.getClientId(), resource));

        when(cache.findAll()).thenReturn(Flux.fromIterable(arr));

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
        when(cache.save(resource)).thenReturn(Mono.just(resource));
        this.webClient.post()
                .uri("/resources/save")
                .body(BodyInserters.fromObject(resource))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody(ClientResources.class);

    }

    @Test
    public void findByClientId(){
        when(cache.findByClientId(resource.getClientId())).thenReturn(Mono.just(resource));
        this.webClient.get()
                .uri("/resources/{clientId}", Collections.singletonMap("clientId",resource.getClientId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClientResources.class);
    }

    @Test
    public void testUpdateClientResources(){
        when(cache.update(resource)).thenReturn(Mono.just(resource));
        when(mongo.findByClientId(resource.getClientId())).thenReturn(Mono.just(resource));
        when(mongo.save(resource)).thenReturn(Mono.just(resource));
        this.webClient.put()
                .uri("/resources/update")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(resource))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody(ClientResources.class);
    }

    @Test
    public void testDeleteClientResources(){
        when(mongo.deleteByClientId(resource.getClientId())).thenReturn(Mono.empty());
        when(cache.deleteByClientId(resource.getClientId())).thenReturn(Mono.empty());
        this.webClient.delete()
                .uri("/resources/delete/{clientId}",  resource.getClientId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}
