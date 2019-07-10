package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.model.Resource;
import com.interswitch.apigateway.repository.MongoProductRepository;
import com.interswitch.apigateway.repository.MongoResourceRepository;
import org.assertj.core.api.Assertions;
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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.util.Collections;

import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.mock;

@ActiveProfiles("dev")
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {ProductController.class})
public class ProductControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoProductRepository mongoProductRepository;

    @MockBean
    private MongoResourceRepository mongoResourceRepository;

    private Disposable disposable;

    private Product product = new Product();

    private Client client;

    private Resource resource;

    @BeforeEach
    public void setup() {
        disposable = mock(Disposable.class);
        client = new Client();
        client.setId("test_client_id");
        client.setClientId("test_client_id");
        resource = new Resource();
        resource.setId("test_resource_id");
        resource.setName("resourceName");
        resource.setMethod("GET");
        resource.setPath("/path");
        product = new Product();
        product.setId("test_product_id");
        product.setName("productName");
        product.setDocumentation("http://interswitch/docs");
        product.addResource(resource);
        product.addClient(client);
    }

    @Test
    public void testGetAll(){
        when(mongoProductRepository.findAll()).thenReturn(Flux.just(product));
        this.webClient.get()
                .uri("/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Product.class);
    }

    @Test
    public void testSave(){
        when(mongoProductRepository.save(product)).thenReturn(Mono.just(product));
        this.webClient.post()
                .uri("/products")
                .body(BodyInserters.fromObject(product))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Product.class);
    }

    @Test
    public void findById(){
        when(mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        this.webClient.get()
                .uri("/products/{productId}", Collections.singletonMap("productId",product.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> Assertions.assertThat(response.getResponseBody()).isNotNull());
    }

    @Test
    public void testUpdate(){
        when(this.mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(this. mongoProductRepository.save(product)).thenReturn(Mono.just(product));
        this.webClient.put()
                .uri("/products")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(product))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Product.class);
    }

    @Test
    public void testDelete(){
        when(mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(mongoProductRepository.deleteById(product.getId())).thenReturn(Mono.empty());
        this.webClient.delete()
                .uri("/products/{productId}",  product.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void testGetResources(){
        when(mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        this.webClient.get()
                .uri("/products/{productId}/resources", product.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Resource.class);
    }

    @Test
    public void testSaveResource(){
        Resource r = new Resource();
        r.setId("testresourceId");
        r.setName("testresourceName");
        r.setMethod("POST");
        r.setPath("/path");
        when(mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(mongoProductRepository.save(product)).thenReturn(Mono.just(product));
        when(mongoResourceRepository.save(r)).thenReturn(Mono.just(r));
        this.webClient.post()
                .uri("/products/{productId}/resources", product.getId())
                .body(BodyInserters.fromObject(r))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class);
    }

    @Test
    public void findResourceById(){
        when(mongoResourceRepository.findById(resource.getId())).thenReturn(Mono.just(resource));
        this.webClient.get()
                .uri("/products/{productId}/resources/{resourceId}", product.getId(), resource.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Resource.class);
    }

    @Test
    public void testUpdateResource(){
        when(mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(mongoResourceRepository.findById(resource.getId())).thenReturn(Mono.just(resource));
        when(mongoProductRepository.save(product)).thenReturn(Mono.just(product));
        when(mongoResourceRepository.save(resource)).thenReturn(Mono.just(resource));
        this.webClient.put()
                .uri("/products/{productId}/resources", product.getId(), resource.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(resource))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class);
    }
    @Test
    public void testDeleteResource(){
        when(mongoResourceRepository.deleteById(resource.getId())).thenReturn(Mono.empty());
        when(mongoResourceRepository.findById(resource.getId())).thenReturn(Mono.just(resource));
        when(mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(mongoProductRepository.save(product)).thenReturn(Mono.just(product));
        this.webClient.delete()
                .uri("/products/{productId}/resources/{resourceId}", product.getId(), resource.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class);
    }
}
