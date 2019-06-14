package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.repository.MongoClientRepository;
import com.interswitch.apigateway.repository.MongoProductRepository;
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
@ContextConfiguration(classes = {ClientController.class})
public class ClientControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoClientRepository mongoClientRepository;

    @MockBean
    private MongoProductRepository mongoProductRepository;

    private Client client;

    private Product product;

    @BeforeEach
    public void setup() {
        product = new Product();
        product.setId("test_product_id");
        client = new Client() ;
        client.setId("test_client_id");
        client.setClientId("test_client_id");
        client.addProduct(product);
    }

    @Test
    public void testGetAll(){
        when(mongoClientRepository.findAll()).thenReturn(Flux.fromIterable(Collections.singletonList(client)));

        this.webClient.get()
                .uri("/clients")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Client.class);

    }

    @Test
    public void testSave(){
        when(mongoClientRepository.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        when(mongoClientRepository.save(client)).thenReturn(Mono.just(client));
        this.webClient.post()
                .uri("/clients")
                .body(BodyInserters.fromObject(client))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Client.class);

    }

    @Test
    public void findByClientId(){
        when(mongoClientRepository.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        this.webClient.get()
                .uri("/clients/{clientId}", client.getClientId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class);
    }

    @Test
    public void testUpdate(){
        when(mongoClientRepository.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        when(mongoClientRepository.save(client)).thenReturn(Mono.just(client));
        this.webClient.put()
                .uri("/clients")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(client))
                .exchange().expectStatus().isOk()
                .expectBody(Client.class);
    }

    @Test
    public void testDelete(){
        when(mongoClientRepository.deleteById(client.getId())).thenReturn(Mono.empty());
        when(mongoClientRepository.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        this.webClient.delete()
                .uri("/clients/{clientId}",  client.getClientId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void testAssignProduct(){
        Product p = new Product();
        p.setId("testProductId");
        when(mongoClientRepository.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        when(mongoClientRepository.save(client)).thenReturn(Mono.just(client));
        when(mongoProductRepository.findById(p.getId())).thenReturn(Mono.just(p));
        when(mongoProductRepository.save(p)).thenReturn(Mono.just(p));
        this.webClient.post()
                .uri("/clients/{clientId}/products/{productId}",  client.getClientId(), p.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class);
        ;
    }

    @Test
    public void testUnassignProduct(){
        when(mongoClientRepository.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        when(mongoClientRepository.save(client)).thenReturn(Mono.just(client));
        when(mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(mongoProductRepository.save(product)).thenReturn(Mono.just(product));
        this.webClient.delete()
                .uri("/clients/{clientId}/products/{productId}",  client.getClientId(), product.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class);
        ;
    }

    @Test
    public void testGetAssignedProducts(){
        when(mongoClientRepository.findByClientId(client.getClientId())).thenReturn(Mono.just(client));
        this.webClient.get()
                .uri("/clients/{clientId}/products",  client.getClientId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class);
    }
}
