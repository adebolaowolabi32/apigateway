package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.model.Project;
import com.interswitch.apigateway.model.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.Collections;

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
        product.setId("test_product_id");
        product.setName("test_product_name");
        product.setDocumentation("http://docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(product.getName()).isEqualTo(savedProduct.getName());
        }).expectComplete().verify();

    }

    @Test
    public void testFindAll(){
        Product p1 = new Product();
        p1.setId("test_product_one");
        p1.setName("test_product_one");
        p1.setDocumentation("http://docs");
        Product p2 = new Product();
        p2.setId("test_product_two");
        p2.setName("test_product_two");
        p2.setDocumentation("http://docs");
        mongoProductRepository.save(p1).block();
        mongoProductRepository.save(p2).block();
        StepVerifier.create(mongoProductRepository.findAll()).expectNextCount(2);
    }
    @Test
    public void testUpdate(){
        Resource resource = new Resource();
        resource.setId("test_resource_id");
        resource.setName("test_resource_name");
        resource.setMethod(HttpMethod.GET);
        resource.setPath("/path");
        Product product = new Product();
        product.setId("test_product_id");
        product.setName("test_product_name");
        product.setDocumentation("http://docs");
        product.addResource(resource);
        Product savedProduct = mongoProductRepository.save(product).block();
        savedProduct.setName("test_product");
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
        product.setId("test_product_id");
        product.setName("test_product_name");
        product.setDocumentation("http://docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        mongoProductRepository.deleteById(savedProduct.getId()).block();
        StepVerifier.create(mongoProductRepository.findById(savedProduct.getId())).expectComplete().verify();
    }

    @Test
    public void testAddResource(){
        Product product = new Product();
        product.setId("test_product_id");
        product.setName("test_product_name");
        product.setDocumentation("http://docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        Resource resource = new Resource();
        resource.setId("test_resource_id");
        resource.setName("test_resource_name");
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
        product.setId("test_product_id");
        product.setName("test_product_name");
        product.setDocumentation("http://docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        Resource resource = new Resource();
        resource.setId("test_resource_id");
        resource.setName("test_resource_name");
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
    public void testAddProject() {
        Product product = new Product();
        product.setId("test_product_id");
        product.setName("test_product_name");
        product.setDocumentation("http://docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        Project project = new Project();
        project.setId("testProjectOne");
        project.setName("testProjectName");
        project.setAuthorizedGrantTypes(Collections.emptySet());
        project.setType(Project.Type.web);
        project.setDescription("testProjectDescription");
        savedProduct.addProject(project);
        mongoProductRepository.save(savedProduct).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(product.getName()).isEqualTo(savedProduct.getName());
            assertThat(p.getProjects()).hasSize(1);
        }).expectComplete().verify();

    }
    @Test
    public void testRemoveProject() {
        Product product = new Product();
        product.setId("product_id");
        product.setName("product_name");
        product.setDocumentation("http://docs");
        Product savedProduct = mongoProductRepository.save(product).block();
        Project project = new Project();
        project.setId("testProjectOne");
        project.setName("testProjectName");
        project.setAuthorizedGrantTypes(Collections.emptySet());
        project.setType(Project.Type.web);
        project.setDescription("testProjectDescription");
        savedProduct.addProject(project);
        Product updatedProduct = mongoProductRepository.save(savedProduct).block();
        updatedProduct.removeProject(project);
        mongoProductRepository.save(updatedProduct).block();
        StepVerifier.create(mongoProductRepository.findById(product.getId())).assertNext(p -> {
            assertThat(p.getName()).isEqualTo(product.getName()).isEqualTo(savedProduct.getName());
            assertThat(p.getProjects()).isEmpty();
        }).expectComplete().verify();
    }
}