package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoConfigRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    MongoConfigRepository repository;

    @Test
    public void testSave() {
        Config config = new Config();
        config.setId("testConfig");
        config.setRouteId("testRoute");
        config.setSandbox(URI.create("https://twitter.com"));
        config.setUat(URI.create("https://google.com"));
        repository.save(config).block();
        StepVerifier.create(repository.findById(config.getId())).assertNext(c -> {
            assertThat(c.getRouteId()).isEqualTo(config.getRouteId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindById() {
        Config config = new Config();
        config.setId("testConfig");
        config.setRouteId("testRoute");
        config.setSandbox(URI.create("https://twitter.com"));
        config.setUat(URI.create("https://google.com"));
        Config savedConfig = repository.save(config).block();
        StepVerifier.create(repository.findById(config.getId())).assertNext(c -> {
            assertThat(c.getRouteId()).isEqualTo(config.getRouteId()).isEqualTo(savedConfig.getRouteId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindByRouteId() {
        Config config = new Config();
        config.setId("testConfig");
        config.setRouteId("testRoute");
        config.setSandbox(URI.create("https://twitter.com"));
        config.setUat(URI.create("https://google.com"));
        Config savedConfig = repository.save(config).block();
        StepVerifier.create(repository.findByRouteId(config.getRouteId())).assertNext(c -> {
            assertThat(c.getId()).isEqualTo(config.getId()).isEqualTo(savedConfig.getId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindAll() {
        Config config = new Config();
        config.setId("testConfig");
        config.setRouteId("testRoute");
        config.setSandbox(URI.create("https://twitter.com"));
        config.setUat(URI.create("https://google.com"));
        Config config2 = new Config();
        config2.setId("testConfig2");
        config2.setRouteId("testRoute2");
        config2.setSandbox(URI.create("https://twitter.com"));
        config2.setUat(URI.create("https://google.com"));
        repository.save(config).block();
        repository.save(config2).block();
        StepVerifier.create(repository.findAll()).expectNextCount(2);
    }

    @Test
    public void testDelete() {
        Config config = new Config();
        config.setId("testConfig");
        config.setRouteId("testRoute");
        config.setSandbox(URI.create("https://twitter.com"));
        config.setUat(URI.create("https://google.com"));
        Config savedConfig = repository.save(config).block();
        repository.deleteById(savedConfig.getId()).block();
        StepVerifier.create(repository.findById(savedConfig.getId())).expectComplete().verify();
    }
}
