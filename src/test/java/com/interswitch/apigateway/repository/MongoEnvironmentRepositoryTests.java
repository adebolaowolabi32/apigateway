package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Environment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoEnvironmentRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    MongoConfigRepository repository;

    @Test
    public void testSave() {
        Environment environment = new Environment();
        environment.setId("testConfig");
        environment.setRouteId("testRoute");
        environment.setSandbox(URI.create("https://twitter.com"));
        environment.setUat(URI.create("https://google.com"));
        repository.save(environment).block();
        StepVerifier.create(repository.findById(environment.getId())).assertNext(c -> {
            assertThat(c.getRouteId()).isEqualTo(environment.getRouteId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindById() {
        Environment environment = new Environment();
        environment.setId("testConfig");
        environment.setRouteId("testRoute");
        environment.setSandbox(URI.create("https://twitter.com"));
        environment.setUat(URI.create("https://google.com"));
        Environment savedEnvironment = repository.save(environment).block();
        StepVerifier.create(repository.findById(environment.getId())).assertNext(c -> {
            assertThat(c.getRouteId()).isEqualTo(environment.getRouteId()).isEqualTo(savedEnvironment.getRouteId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindByRouteId() {
        Environment environment = new Environment();
        environment.setId("testConfig");
        environment.setRouteId("testRoute");
        environment.setSandbox(URI.create("https://twitter.com"));
        environment.setUat(URI.create("https://google.com"));
        Environment savedEnvironment = repository.save(environment).block();
        StepVerifier.create(repository.findByRouteId(environment.getRouteId())).assertNext(c -> {
            assertThat(c.getId()).isEqualTo(environment.getId()).isEqualTo(savedEnvironment.getId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindAll() {
        Environment environment = new Environment();
        environment.setId("testConfig");
        environment.setRouteId("testRoute");
        environment.setSandbox(URI.create("https://twitter.com"));
        environment.setUat(URI.create("https://google.com"));
        Environment environment2 = new Environment();
        environment2.setId("testConfig2");
        environment2.setRouteId("testRoute2");
        environment2.setSandbox(URI.create("https://twitter.com"));
        environment2.setUat(URI.create("https://google.com"));
        repository.save(environment).block();
        repository.save(environment2).block();
        StepVerifier.create(repository.findAll()).expectNextCount(2);
    }

    @Test
    public void testDelete() {
        Environment environment = new Environment();
        environment.setId("testConfig");
        environment.setRouteId("testRoute");
        environment.setSandbox(URI.create("https://twitter.com"));
        environment.setUat(URI.create("https://google.com"));
        Environment savedEnvironment = repository.save(environment).block();
        repository.deleteById(savedEnvironment.getId()).block();
        StepVerifier.create(repository.findById(savedEnvironment.getId())).expectComplete().verify();
    }
}
