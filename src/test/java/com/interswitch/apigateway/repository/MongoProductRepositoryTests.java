package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.model.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoProductRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    MongoProductRepository mongoProductRepository;
    @Autowired
    MongoResourceRepository mongoResourceRepository;

    @Test
    public void testFindById(){
        Product product = new Product();
        product.setId("testProductId");
        product.setName("testProductName");
        product.setDocumentation("/docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(product.getName()).isEqualTo(savedProduct.getName());
        }).expectComplete().verify();

    }

    @Test
    public void testFindAll(){
        Product p1 = new Product();
        p1.setId("testProductOne");
        p1.setName("testProductOne");
        p1.setDocumentation("/docs");
        Product p2 = new Product();
        p2.setId("testProductTwo");
        p2.setName("testProductTwo");
        p2.setDocumentation("/docs");
        mongoProductRepository.save(p1).block();
        mongoProductRepository.save(p2).block();
        StepVerifier.create(mongoProductRepository.findAll()).expectNextCount(2);
    }
    @Test
    public void testUpdate(){
        Resource resource = new Resource();
        resource.setId("testResourceId");
        resource.setName("testResourceName");
        resource.setMethod(HttpMethod.GET);
        resource.setPath("/path");
        Product product = new Product();
        product.setId("testProductId");
        product.setName("testProductName");
        product.setDocumentation("/docs");
        product.addResource(resource);
        Product savedProduct = mongoProductRepository.save(product).block();
        savedProduct.setName("testproduct");
        savedProduct.setDocumentation("/books");
        mongoProductRepository.save(product).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(savedProduct.getName());
            assertThat(p.getDocumentation()).isEqualTo(savedProduct.getDocumentation());
        }).expectComplete().verify();
    }
    @Test
    public void testDelete(){
        Product product = new Product();
        product.setId("testProductId");
        product.setName("testProductName");
        product.setDocumentation("/docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        mongoProductRepository.deleteById(savedProduct.getId()).block();
        StepVerifier.create(mongoProductRepository.findById(savedProduct.getId())).expectComplete().verify();
    }

    @Test
    public void testAddResource(){
        Product product = new Product();
        product.setId("testProductId");
        product.setName("testProductName");
        product.setDocumentation("/docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        Resource resource = new Resource();
        resource.setId("testResourceId");
        resource.setName("testResourceName");
        resource.setMethod(HttpMethod.GET);
        resource.setPath("/path");
        resource.setProduct(product);
        Resource savedResource = mongoResourceRepository.save(resource).block();
        savedProduct.addResource(resource);
        Product updatedProduct = mongoProductRepository.save(savedProduct).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(savedProduct.getName()).isEqualTo(updatedProduct.getName());
            assertThat(p.getResources()).hasSize(1);
        }).expectComplete().verify();
        StepVerifier.create(mongoResourceRepository.findById(resource.getId())).assertNext(r -> {
            assertThat(r.getName()).isEqualTo(savedResource.getName());
            assertThat(r.getProduct()).isNotNull();
        }).expectComplete().verify();

    }
    @Test
    public void testRemoveResource(){
        Product product = new Product();
        product.setId("testProductId");
        product.setName("testProductName");
        product.setDocumentation("/docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        Resource resource = new Resource();
        resource.setId("testResourceId");
        resource.setName("testResourceName");
        resource.setMethod(HttpMethod.GET);
        resource.setPath("/path");
        resource.setProduct(product);
        Resource savedResource = mongoResourceRepository.save(resource).block();
        savedProduct.addResource(resource);
        Product updatedProduct = mongoProductRepository.save(savedProduct).block();
        updatedProduct.removeResource(resource);
        mongoResourceRepository.delete(savedResource).block();
        mongoProductRepository.save(updatedProduct).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(savedProduct.getName()).isEqualTo(updatedProduct.getName());
            assertThat(p.getResources()).isEmpty();
        }).expectComplete().verify();
        StepVerifier.create(mongoResourceRepository.findById(resource.getId())).expectComplete().verify();
    }

    @Test
    public void testAddClient(){
        Product product = new Product();
        product.setId("testProductId");
        product.setName("testProductName");
        product.setDocumentation("/docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        Client client = new Client();
        client.setId("testClientOne");
        client.setClientId("testClientOne");
        savedProduct.addClient(client);
        mongoProductRepository.save(savedProduct).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(product.getName()).isEqualTo(savedProduct.getName());
            assertThat(p.getClients()).hasSize(1);
        }).expectComplete().verify();

    }
    @Test
    public void testRemoveClient(){
        Product product = new Product();
        product.setId("productId");
        product.setName("productName");
        product.setDocumentation("/docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        Client client = new Client();
        client.setId("testClientOne");
        client.setClientId("testClientOne");
        savedProduct.addClient(client);
        Product updatedProduct = mongoProductRepository.save(savedProduct).block();
        updatedProduct.removeClient(client);
        mongoProductRepository.save(updatedProduct).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(product.getName()).isEqualTo(savedProduct.getName());
            assertThat(p.getClients()).isEmpty();
        }).expectComplete().verify();
    }
}
