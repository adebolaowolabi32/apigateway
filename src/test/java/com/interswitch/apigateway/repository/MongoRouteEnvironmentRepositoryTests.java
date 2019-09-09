package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.RouteEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoRouteEnvironmentRepositoryTests extends AbstractMongoRepositoryTests {

    @Autowired
    MongoRouteEnvironmentRepository repository;

    @Test
    public void testSave() {
        RouteEnvironment routeEnvironment = new RouteEnvironment();
        routeEnvironment.setId("testConfig");
        routeEnvironment.setRouteId("testRoute");
        routeEnvironment.setTestURL("https://twitter.com");
        repository.save(routeEnvironment).block();
        StepVerifier.create(repository.findById(routeEnvironment.getId())).assertNext(c -> {
            assertThat(c.getRouteId()).isEqualTo(routeEnvironment.getRouteId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindById() {
        RouteEnvironment routeEnvironment = new RouteEnvironment();
        routeEnvironment.setId("testConfig");
        routeEnvironment.setRouteId("testRoute");
        routeEnvironment.setTestURL("https://twitter.com");
        RouteEnvironment savedRouteEnvironment = repository.save(routeEnvironment).block();
        StepVerifier.create(repository.findById(routeEnvironment.getId())).assertNext(c -> {
            assertThat(c.getRouteId()).isEqualTo(routeEnvironment.getRouteId()).isEqualTo(savedRouteEnvironment.getRouteId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindByRouteId() {
        RouteEnvironment routeEnvironment = new RouteEnvironment();
        routeEnvironment.setId("testConfig");
        routeEnvironment.setRouteId("testRoute");
        routeEnvironment.setTestURL("https://twitter.com");
        RouteEnvironment savedRouteEnvironment = repository.save(routeEnvironment).block();
        StepVerifier.create(repository.findByRouteId(routeEnvironment.getRouteId())).assertNext(c -> {
            assertThat(c.getId()).isEqualTo(routeEnvironment.getId()).isEqualTo(savedRouteEnvironment.getId());
        }).expectComplete().verify();
    }

    @Test
    public void testFindAll() {
        RouteEnvironment routeEnvironment = new RouteEnvironment();
        routeEnvironment.setId("testConfig");
        routeEnvironment.setRouteId("testRoute");
        routeEnvironment.setTestURL("https://twitter.com");
        RouteEnvironment routeEnvironment2 = new RouteEnvironment();
        routeEnvironment2.setId("testConfig2");
        routeEnvironment2.setRouteId("testRoute2");
        routeEnvironment2.setTestURL("https://twitter.com");
        repository.save(routeEnvironment).block();
        repository.save(routeEnvironment2).block();
        StepVerifier.create(repository.findAll()).expectNextCount(2);
    }

    @Test
    public void testDelete() {
        RouteEnvironment routeEnvironment = new RouteEnvironment();
        routeEnvironment.setId("testConfig");
        routeEnvironment.setRouteId("testRoute");
        routeEnvironment.setTestURL("https://twitter.com");
        RouteEnvironment savedRouteEnvironment = repository.save(routeEnvironment).block();
        repository.deleteById(savedRouteEnvironment.getId()).block();
        StepVerifier.create(repository.findById(savedRouteEnvironment.getId())).expectComplete().verify();
    }
}
