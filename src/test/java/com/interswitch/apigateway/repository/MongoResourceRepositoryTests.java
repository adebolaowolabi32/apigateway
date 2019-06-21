package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.model.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoResourceRepositoryTests extends AbstractMongoRepositoryTests {
    @Autowired
    MongoResourceRepository mongoResourceRepository;

    @Test
    public void testFindById(){
        Resource resource = new Resource();
        resource.setId("resourceId");
        resource.setName("resourceName");
        resource.setMethod("GET");
        resource.setPath("/path");
        Product product = new Product();
        product.setId("testProductId");
        product.setName("productNameTwo");
        resource.setProduct(product);
        Resource savedResource = mongoResourceRepository.save(resource).block();
        StepVerifier.create(mongoResourceRepository.findById(savedResource.getId())).assertNext(r -> {
            assertThat(r.getId()).isEqualTo("resourceId");
            assertThat(r.getName()).isEqualTo("resourceName");
            assertThat(r.getMethod()).isEqualTo("GET");
            assertThat(r.getPath()).isEqualTo("/path");
            assertThat(r.getProduct()).isNotNull();
        }).expectComplete().verify();

    }

    @Test
    public void testFindAll(){
        Product product = new Product();
        product.setId("product");
        product.setName("product");
        Resource r1 = new Resource();
        r1.setId("resourceOne");
        r1.setName("resourceOne");
        r1.setMethod("GET");
        r1.setPath("/path");
        r1.setProduct(product);
        Resource r2 = new Resource();
        r2.setId("resourceTwo");
        r2.setName("resourceTwo");
        r2.setMethod("POST");
        r2.setPath("/path");
        r2.setProduct(product);
        StepVerifier.create(mongoResourceRepository.findAll()).expectNextCount(2);
    }

    @Test
    public void testUpdate(){
        Resource resource = new Resource();
        resource.setId("resourceId");
        resource.setName("resourceName");
        resource.setMethod("GET");
        resource.setPath("/path");
        Product product = new Product();
        product.setId("testProductId");
        product.setName("productNameTwo");
        resource.setProduct(product);
        Resource savedResource = mongoResourceRepository.save(resource).block();
        savedResource.setName("resource");
        mongoResourceRepository.save(savedResource).block();
        StepVerifier.create(mongoResourceRepository.findById(resource.getId())).assertNext(r -> {
            assertThat(r.getId()).isEqualTo("resourceId");
            assertThat(r.getName()).isEqualTo("resource");
            assertThat(r.getProduct()).isNotNull();
        }).expectComplete().verify();
    }
    @Test
    public void testDelete(){
        Resource resource = new Resource();
        resource.setId("resourceId");
        resource.setName("resourceName");
        resource.setMethod("GET");
        resource.setPath("/path");
        Resource savedResource = mongoResourceRepository.save(resource).block();
        mongoResourceRepository.deleteById(savedResource.getId()).block();
        StepVerifier.create(mongoResourceRepository.findById(savedResource.getId())).expectComplete().verify();
    }

}
