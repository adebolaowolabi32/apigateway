package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.config.CacheConfig;
import com.interswitch.apigateway.model.ClientResources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataRedisTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {CacheConfig.class, MongoClientResourcesRepository.class, ClientResourcesRepository.class})
public class ClientResourceRepositoryTests {

    @Autowired
    ClientResourcesRepository clientResourcesRepository;

    @MockBean
    MongoClientResourcesRepository mongoClientResourcesRepository;

    private List testresourceIds;
    private ClientResources clientResources;
    private String  clientId = "testclientresource";


    @BeforeEach
    public void setUp() {
        testresourceIds = new ArrayList();
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        clientResources = new ClientResources("id",clientId,testresourceIds);
        clientResourcesRepository.save(clientResources).block();
    }

    @AfterEach
    public void delete(){
        clientResourcesRepository.deleteByClientId(clientResources.getClientId()).block();
    }

    @Test
    public void testGetClientResources() {
        StepVerifier.create(clientResourcesRepository.findAll()).expectNextCount(1);
    }
    @Test
    public void testFindClientResources() {
        Mono<ClientResources> clientResourceMono = clientResourcesRepository.findByClientId(clientResources.getClientId());

        StepVerifier.create(clientResourceMono).assertNext(r -> {
            assertThat(r.getId()).isEqualTo("id");
            assertThat(r.getClientId()).isEqualTo(clientId);
            assertThat(r.getResourceIds()).hasSize(2);
        }).expectComplete().verify();
    }

    @Test
    public void testUpdateClientResources() {
        testresourceIds.remove("passport/oauth/token");
        clientResources.setResourceIds(testresourceIds);
        clientResourcesRepository.update(clientResources).block();
        Mono<ClientResources> clientResourceMono = clientResourcesRepository.findByClientId(clientResources.getClientId());

        StepVerifier.create(clientResourceMono).assertNext(r -> {
            assertThat(r.getResourceIds()).hasSize(1);
        }).expectComplete().verify();
    }
}