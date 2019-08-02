package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoClientRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    MongoClientRepository mongoClientRepository;

    @Test
    public void testCreate(){
        Client client = new Client();
        client.setId("testClientOne");
        client.setClientId("testClientOne");
        mongoClientRepository.save(client).block();
        StepVerifier.create(mongoClientRepository.findById(client.getId())).assertNext(c -> {
            assertThat(c.getClientId()).isEqualTo(client.getClientId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindById(){
        Client client = new Client();
        client.setId("testClientOne");
        client.setClientId("testClientOne");
        Client savedClient = mongoClientRepository.save(client).block();
        StepVerifier.create(mongoClientRepository.findById(client.getId())).assertNext(c -> {
            assertThat(c.getClientId()).isEqualTo(client.getClientId()).isEqualTo(savedClient.getClientId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindByClientId(){
        Client client = new Client();
        client.setId("testClientOne");
        client.setClientId("testClientOne");
        Client savedClient = mongoClientRepository.save(client).block();
        StepVerifier.create(mongoClientRepository.findByClientId(client.getClientId())).assertNext(c->{
            assertThat(c.getId()).isEqualTo(client.getId()).isEqualTo(savedClient.getId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindAll(){
        Client c1 = new Client();
        c1.setId("testClientOne");
        c1.setClientId("testClientOne");
        Client c2 = new Client();
        c2.setId("testClientTwo");
        c2.setClientId("testClientTwo");
        Product product = new Product();
        product.setId("product_id");
        product.setName("product_name");
        c2.addProduct(product);
        mongoClientRepository.save(c1).block();
        mongoClientRepository.save(c2).block();
        StepVerifier.create(mongoClientRepository.findAll()).expectNextCount(2);
    }

    @Test
    public void testDelete(){
        Client client = new Client();
        client.setId("testClientOne");
        client.setClientId("testClientOne");
        Client savedClient = mongoClientRepository.save(client).block();
        mongoClientRepository.deleteById(savedClient.getId()).block();
        StepVerifier.create(mongoClientRepository.findById(savedClient.getId())).expectComplete().verify();
    }

    @Test
    public void testAssignProduct(){
        Client client = new Client();
        client.setId("testClientOne");
        client.setClientId("testClientOne");
        Client savedClient = mongoClientRepository.save(client).block();
        Product product = new Product();
        product.setId("test_product_one");
        product.setName("test_product_one");
        savedClient.addProduct(product);
        Client updatedClient = mongoClientRepository.save(savedClient).block();
        StepVerifier.create(mongoClientRepository.findById(client.getId())).assertNext(c -> {
            assertThat(c.getClientId()).isEqualTo(client.getClientId()).isEqualTo(savedClient.getClientId()).isEqualTo(updatedClient.getClientId());
            assertThat(c.getProducts()).hasSize(1);
        }).expectComplete().verify();
    }
    @Test
    public void testRemoveProduct(){
        Client client = new Client();
        client.setId("testClientOne");
        client.setClientId("testClientOne");
        Client savedClient = mongoClientRepository.save(client).block();
        Product product = new Product();
        product.setId("test_product_one");
        product.setName("test_product_one");
        savedClient.addProduct(product);
        Client updatedClient = mongoClientRepository.save(savedClient).block();
        updatedClient.removeProduct(product);
        mongoClientRepository.save(updatedClient).block();
        StepVerifier.create(mongoClientRepository.findById(client.getId())).assertNext(c -> {
            assertThat(c.getClientId()).isEqualTo(savedClient.getClientId()).isEqualTo(updatedClient.getClientId());
            assertThat(c.getProducts()).isEmpty();
        }).expectComplete().verify();
    }
}
