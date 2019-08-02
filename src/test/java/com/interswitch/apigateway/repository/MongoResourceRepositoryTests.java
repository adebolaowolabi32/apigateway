package com.interswitch.apigateway.repository;

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
public class MongoResourceRepositoryTests extends AbstractMongoRepositoryTests {
    @Autowired
    MongoResourceRepository mongoResourceRepository;

    @Test
    public void testFindById(){
        Resource resource = new Resource();
        resource.setId("testResourceId");
        resource.setName("testResourceName");
        resource.setMethod(HttpMethod.GET);
        resource.setPath("/path");
        Product product = new Product();
        product.setId("testProductId");
        product.setName("testproductName");
        resource.setProduct(product);
        Resource savedResource = mongoResourceRepository.save(resource).block();
        StepVerifier.create(mongoResourceRepository.findById(savedResource.getId())).assertNext(r -> {
            assertThat(r.getId()).isEqualTo("testResourceId");
            assertThat(r.getName()).isEqualTo("testResourceName");
            assertThat(r.getMethod()).isEqualTo("GET");
            assertThat(r.getPath()).isEqualTo("/path");
            assertThat(r.getProduct()).isNotNull();
        }).expectComplete().verify();

    }

    @Test
    public void testFindAll(){
        Product product = new Product();
        product.setId("testProductId");
        product.setName("testproductName");
        Resource r1 = new Resource();
        r1.setId("testResourceOne");
        r1.setName("testResourceOne");
        r1.setMethod(HttpMethod.GET);
        r1.setPath("/path");
        r1.setProduct(product);
        Resource r2 = new Resource();
        r2.setId("testResourceTwo");
        r2.setName("testResourceTwo");
        r2.setMethod(HttpMethod.POST);
        r2.setPath("/path");
        r2.setProduct(product);
        mongoResourceRepository.save(r1).block();
        mongoResourceRepository.save(r2).block();
        StepVerifier.create(mongoResourceRepository.findAll()).expectNextCount(2);
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
        product.setName("testproductName");
        resource.setProduct(product);
        Resource savedResource = mongoResourceRepository.save(resource).block();
        savedResource.setName("testResource");
        mongoResourceRepository.save(savedResource).block();
        StepVerifier.create(mongoResourceRepository.findById(resource.getId())).assertNext(r -> {
            assertThat(r.getId()).isEqualTo("testResourceId");
            assertThat(r.getName()).isEqualTo("testResource");
            assertThat(r.getProduct()).isNotNull();
        }).expectComplete().verify();
    }
    @Test
    public void testDelete(){
        Resource resource = new Resource();
        resource.setId("testResourceId");
        resource.setName("testResourceName");
        resource.setMethod(HttpMethod.GET);
        resource.setPath("/path");
        Resource savedResource = mongoResourceRepository.save(resource).block();
        mongoResourceRepository.deleteById(savedResource.getId()).block();
        StepVerifier.create(mongoResourceRepository.findById(savedResource.getId())).expectComplete().verify();
    }

}
