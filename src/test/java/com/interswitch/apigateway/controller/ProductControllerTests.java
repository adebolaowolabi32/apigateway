package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.repository.MongoProductRepository;
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
@WebFluxTest(value = {ProductController.class}, excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveManagementWebSecurityAutoConfiguration.class,
        ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {MongoProductRepository.class, ProductController.class})
public class ProductControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoProductRepository mongo;


    private List testproductsIds = new ArrayList();
    private Product products = new Product();

    @BeforeEach
    public void setup() throws URISyntaxException {
        testproductsIds.add("passport/oauth/token");
        testproductsIds.add("passport/oauth/authorize");
        products = new Product("id","productId",testproductsIds,"Payment","Description");
    }
    @Test
    public void testGetProducts(){
        when(mongo.findAll()).thenReturn(Flux.just(products));
        this.webClient.get()
                .uri("/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Product.class);
    }

    @Test
    public void testSaveProducts(){
        when(mongo.save(products)).thenReturn(Mono.just(products));
        this.webClient.post()
                .uri("/products/save")
                .body(BodyInserters.fromObject(products))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Product.class);
    }

    @Test
    public void findByProductId(){
        when(mongo.findByProductId(products.getProductId())).thenReturn(Mono.just(products));
        this.webClient.get()
                .uri("/products/{productId}", Collections.singletonMap("productId",products.getProductId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> Assertions.assertThat(response.getResponseBody()).isNotNull());
    }

    @Test
    public void testUpdateProducts(){
        when(this.mongo.findByProductId(products.getProductId())).thenReturn(Mono.just(products));
        when(this. mongo.save(products)).thenReturn(Mono.just(products));
        this.webClient.put()
                .uri("/products/update")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(products))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Product.class);
    }
    @Test
    public void testDeleteProducts(){
        when(mongo.deleteById(products.getId())).thenReturn(Mono.empty());
        when(mongo.findById(products.getId())).thenReturn(Mono.just(products));
        this.webClient.delete()
                .uri("/products/delete/{id}",  Collections.singletonMap("id",products.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}
