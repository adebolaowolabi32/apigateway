package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.AccessLogs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoAccessLogsRepositoryTests extends AbstractMongoRepositoryTests{

    @Autowired
    MongoAccessLogsRepository mongoAccessLogsRepository;

    @Test
    public void testFindAll(){
        AccessLogs a1 = new AccessLogs();
        a1.setId("accessLogs1");
        a1.setAction(AccessLogs.Action.CREATE);
        a1.setEntity(AccessLogs.Entity.PRODUCT);
        a1.setEntityId("productId");
        a1.setApi("/products");
        a1.setTimestamp(LocalDateTime.now());
        a1.setUsername("user.name");
        a1.setStatus(AccessLogs.Status.SUCCESSFUL);

        AccessLogs a2 = new AccessLogs();
        a2.setId("accessLogs2");
        a2.setAction(AccessLogs.Action.UPDATE);
        a2.setEntity(AccessLogs.Entity.PRODUCT);
        a2.setEntityId("productId");
        a2.setApi("/products");
        a2.setTimestamp(LocalDateTime.now());
        a2.setUsername("user.name");
        a2.setStatus(AccessLogs.Status.SUCCESSFUL);
        mongoAccessLogsRepository.save(a1).block();
        mongoAccessLogsRepository.save(a2).block();
        StepVerifier.create(mongoAccessLogsRepository.findAll()).expectNextCount(2);
    }

    @Test
    public void testFindById(){
        AccessLogs accessLogs = new AccessLogs();
        accessLogs.setId("accessLogs1");
        accessLogs.setAction(AccessLogs.Action.CREATE);
        accessLogs.setEntity(AccessLogs.Entity.PRODUCT);
        accessLogs.setEntityId("productId");
        accessLogs.setApi("/products");
        accessLogs.setTimestamp(LocalDateTime.now());
        accessLogs.setUsername("user.name");
        accessLogs.setStatus(AccessLogs.Status.SUCCESSFUL);
        AccessLogs savedAccessLogs = mongoAccessLogsRepository.save(accessLogs).block();
        StepVerifier.create(mongoAccessLogsRepository.findById(accessLogs.getId())).assertNext(a -> {
            assertThat(a.getId()).isEqualTo(accessLogs.getId()).isEqualTo(savedAccessLogs.getId());
            assertThat(a.getApi()).isEqualTo(accessLogs.getApi()).isEqualTo(savedAccessLogs.getApi());
            assertThat(a.getEntity()).isEqualTo(accessLogs.getEntity()).isEqualTo(savedAccessLogs.getEntity());
            assertThat(a.getEntityId()).isEqualTo(accessLogs.getEntityId()).isEqualTo(savedAccessLogs.getEntityId());
            assertThat(a.getUsername()).isEqualTo(accessLogs.getUsername()).isEqualTo(savedAccessLogs.getUsername());
            assertThat(a.getAction()).isEqualTo(accessLogs.getAction()).isEqualTo(savedAccessLogs.getAction());
            assertThat(a.getStatus()).isEqualTo(accessLogs.getStatus()).isEqualTo(savedAccessLogs.getStatus()
            );
        }).expectComplete().verify();
    }

    @Test
    public void testDelete(){
        AccessLogs accessLogs = new AccessLogs();
        accessLogs.setId("accessLogs1");
        accessLogs.setAction(AccessLogs.Action.CREATE);
        accessLogs.setEntity(AccessLogs.Entity.PRODUCT);
        accessLogs.setEntityId("productId");
        accessLogs.setApi("/products");
        accessLogs.setTimestamp(LocalDateTime.now());
        accessLogs.setUsername("user.name");
        accessLogs.setStatus(AccessLogs.Status.SUCCESSFUL);
        AccessLogs savedAccessLogs = mongoAccessLogsRepository.save(accessLogs).block();
        mongoAccessLogsRepository.deleteById(accessLogs.getId()).block();
        StepVerifier.create(mongoAccessLogsRepository.findById(savedAccessLogs.getId())).expectComplete().verify();
    }
}
