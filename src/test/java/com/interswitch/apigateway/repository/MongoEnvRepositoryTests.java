package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Env;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoEnvRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    MongoEnvRepository repository;

    @Test
    public void testSave() {
        Env env = new Env();
        env.setId("testConfig");
        env.setRouteId("testRoute");
        env.setSandbox("https://twitter.com");
        env.setUat("https://google.com");
        repository.save(env).block();
        StepVerifier.create(repository.findById(env.getId())).assertNext(c -> {
            assertThat(c.getRouteId()).isEqualTo(env.getRouteId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindById() {
        Env env = new Env();
        env.setId("testConfig");
        env.setRouteId("testRoute");
        env.setSandbox("https://twitter.com");
        env.setUat("https://google.com");
        Env savedEnv = repository.save(env).block();
        StepVerifier.create(repository.findById(env.getId())).assertNext(c -> {
            assertThat(c.getRouteId()).isEqualTo(env.getRouteId()).isEqualTo(savedEnv.getRouteId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindByRouteId() {
        Env env = new Env();
        env.setId("testConfig");
        env.setRouteId("testRoute");
        env.setSandbox("https://twitter.com");
        env.setUat("https://google.com");
        Env savedEnv = repository.save(env).block();
        StepVerifier.create(repository.findByRouteId(env.getRouteId())).assertNext(c -> {
            assertThat(c.getId()).isEqualTo(env.getId()).isEqualTo(savedEnv.getId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindAll() {
        Env env = new Env();
        env.setId("testConfig");
        env.setRouteId("testRoute");
        env.setSandbox("https://twitter.com");
        env.setUat("https://google.com");
        Env env2 = new Env();
        env2.setId("testConfig2");
        env2.setRouteId("testRoute2");
        env2.setSandbox("https://twitter.com");
        env2.setUat("https://google.com");
        repository.save(env).block();
        repository.save(env2).block();
        StepVerifier.create(repository.findAll()).expectNextCount(2);
    }

    @Test
    public void testDelete() {
        Env env = new Env();
        env.setId("testConfig");
        env.setRouteId("testRoute");
        env.setSandbox("https://twitter.com");
        env.setUat("https://google.com");
        Env savedEnv = repository.save(env).block();
        repository.deleteById(savedEnv.getId()).block();
        StepVerifier.create(repository.findById(savedEnv.getId())).expectComplete().verify();
    }
}
