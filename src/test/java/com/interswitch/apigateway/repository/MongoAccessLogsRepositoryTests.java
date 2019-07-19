package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.model.AccessLogs.Action;
import com.interswitch.apigateway.model.AccessLogs.Entity;
import com.interswitch.apigateway.model.AccessLogs.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoAccessLogsRepositoryTests extends AbstractMongoRepositoryTests{

    @Autowired
    private MongoAccessLogsRepository mongoAccessLogsRepository;

    private AccessLogs accessLogs;

    @BeforeEach
    public void setup() {
        accessLogs = new AccessLogs();
        accessLogs.setId("accessLogs1");
        accessLogs.setAction(Action.CREATE);
        accessLogs.setEntity(Entity.PRODUCT);
        accessLogs.setEntityId("productId");
        accessLogs.setApi("/products");
        accessLogs.setTimestamp(LocalDateTime.now());
        accessLogs.setUsername("user.name");
        accessLogs.setClient("client.id");
        accessLogs.setStatus(Status.SUCCESSFUL);
        mongoAccessLogsRepository.save(accessLogs).block();

        accessLogs = new AccessLogs();
        accessLogs.setId("accessLogs2");
        accessLogs.setAction(Action.UPDATE);
        accessLogs.setEntity(Entity.PRODUCT);
        accessLogs.setEntityId("productId");
        accessLogs.setApi("/products");
        accessLogs.setTimestamp(LocalDateTime.now());
        accessLogs.setUsername("user.name.other");
        accessLogs.setClient("client.id");
        accessLogs.setStatus(Status.SUCCESSFUL);
        mongoAccessLogsRepository.save(accessLogs).block();

    }

    @Test
    public void testRetrieveAllPaged() {
        StepVerifier.create(mongoAccessLogsRepository.retrieveAllPaged(PageRequest.of(0, 10))).expectNextCount(2);
    }

    @Test
    public void testQuery() {
        StepVerifier.create(mongoAccessLogsRepository.query("user.name.other", PageRequest.of(0, 10))).expectNextCount(1);
    }

    @Test
    public void testFindAll() {
        StepVerifier.create(mongoAccessLogsRepository.findAll()).expectNextCount(2);
    }

    @Test
    public void testFindById() {
        AccessLogs savedAccessLogs = mongoAccessLogsRepository.save(accessLogs).block();
        StepVerifier.create(mongoAccessLogsRepository.findById(accessLogs.getId())).assertNext(a -> {
            assertThat(a.getId()).isEqualTo(accessLogs.getId()).isEqualTo(savedAccessLogs.getId());
            assertThat(a.getApi()).isEqualTo(accessLogs.getApi()).isEqualTo(savedAccessLogs.getApi());
            assertThat(a.getEntity()).isEqualTo(accessLogs.getEntity()).isEqualTo(savedAccessLogs.getEntity());
            assertThat(a.getEntityId()).isEqualTo(accessLogs.getEntityId()).isEqualTo(savedAccessLogs.getEntityId());
            assertThat(a.getUsername()).isEqualTo(accessLogs.getUsername()).isEqualTo(savedAccessLogs.getUsername());
            assertThat(a.getClient()).isEqualTo(accessLogs.getClient()).isEqualTo(savedAccessLogs.getClient());
            assertThat(a.getAction()).isEqualTo(accessLogs.getAction()).isEqualTo(savedAccessLogs.getAction());
            assertThat(a.getStatus()).isEqualTo(accessLogs.getStatus()).isEqualTo(savedAccessLogs.getStatus());
        }).expectComplete().verify();
    }

    @Test
    public void testDelete(){
        AccessLogs savedAccessLogs = mongoAccessLogsRepository.save(accessLogs).block();
        mongoAccessLogsRepository.deleteById(accessLogs.getId()).block();
        StepVerifier.create(mongoAccessLogsRepository.findById(savedAccessLogs.getId())).expectComplete().verify();
    }
}
