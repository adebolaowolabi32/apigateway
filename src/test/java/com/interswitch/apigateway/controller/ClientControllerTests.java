package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
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
import java.util.*;

import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest(value = {ClientController.class}, excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {ClientMongoRepository.class, ClientCacheRepository.class, ClientController.class})
public class ClientControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ClientMongoRepository mongo;

    @MockBean
    private ClientCacheRepository cache;
    
    private List origins;
    private List resourceIds;
    private Client client;
    private String clientId = "testclientid";

    @BeforeEach
    public void setup() {
        resourceIds = Arrays.asList("passport/oauth/token", "passport/oauth/authorize");
        origins = Arrays.asList("https://qa.interswitchng.com", "http://localhost:3000");
        client = new Client("id", clientId,"Approved", origins, resourceIds);
    }
    
    @Test
    public void testGetAllClients(){
        ArrayList<Map.Entry<String, Client>> listOfClients = new ArrayList<>();
        listOfClients.add(new AbstractMap.SimpleEntry(client.getClientId(), client));

        when(cache.findAll()).thenReturn(Flux.fromIterable(listOfClients));

        this.webClient.get()
                .uri("/clients")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Client.class);

    }

    @Test
    public void testSaveClient(){
        when(mongo.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        when(mongo.save(client)).thenReturn(Mono.just(client));
        when(cache.save(client)).thenReturn(Mono.just(client));
        this.webClient.post()
                .uri("/clients/save")
                .body(BodyInserters.fromObject(client))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(Client.class);

    }

    @Test
    public void findClientByClientId(){
        when(cache.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        this.webClient.get()
                .uri("/clients/{clientId}", client.getClientId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class);
    }

    @Test
    public void testUpdateClient(){
        when(cache.update(client)).thenReturn(Mono.just(client));
        when(mongo.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        when(mongo.save(client)).thenReturn(Mono.just(client));
        this.webClient.put()
                .uri("/clients/update")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(client))
                .exchange()
                .expectBody(Client.class);
    }

    @Test
    public void testDeleteClient(){
        when(mongo.deleteById(client.getId())).thenReturn(Mono.empty());
        when(cache.deleteByClientId(client.getClientId())).thenReturn(Mono.empty());
        when(mongo.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        this.webClient.delete()
                .uri("/clients/delete/{clientId}",  client.getClientId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}
