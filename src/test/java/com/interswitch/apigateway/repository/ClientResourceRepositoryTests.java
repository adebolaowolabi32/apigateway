package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.config.CacheConfig;
import com.interswitch.apigateway.model.ClientResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ActiveProfiles("dev")
@Import(CacheConfig.class)
public class ClientResourceRepositoryTests {

    @Autowired
    ClientResourcesRepository clientResourcesRepository;

    private List testresourceIds;
    private ClientResources clientResources;

    private String  clientId = "testclient";


    @BeforeEach
    public void setUp() {
        clientResourcesRepository = mock(ClientResourcesRepository.class);
        testresourceIds = new ArrayList();
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        clientResources = new ClientResources("id",clientId,testresourceIds);

    }

    @Test
    public void testGetClientResources() {
        StepVerifier.create(clientResourcesRepository.findAll().doOnNext(System.out::println)).expectNextCount(2).verifyComplete();
    }
    @Test
    public void testFindClientResources() {
        clientResourcesRepository.save(clientResources).block();
        clientResourcesRepository.save(clientResources).block();

        Mono<ClientResources> clientResourceMono = clientResourcesRepository.findByClientId(clientResources.getClientId());

        StepVerifier.create(clientResourceMono).assertNext(r -> {
            assertThat(r.getId()).isEqualTo("id");
            assertThat(r.getClientId()).isEqualTo(clientId);
            assertThat(r.getResourceIds()).hasSize(2);
        }).expectComplete().verify();
    }

    @Test
    public void testUpdateClientResources() {
        clientResources.setClientId("newClientID");
        clientResourcesRepository.update(clientResources).block();

        Mono<ClientResources> clientResourceMono = clientResourcesRepository.findByClientId(clientResources.getClientId());

        StepVerifier.create(clientResourceMono).assertNext(r -> {
            assertThat(r.getClientId()).isEqualTo("newClientID");
        }).expectComplete().verify();
    }
    
    @Test
    public void testDeleteClientResources() {
        clientResourcesRepository.deleteByClientId(clientResources.getId()).block();

        Mono<ClientResources> clientResourceMono = clientResourcesRepository.findByClientId(clientResources.getClientId());

        StepVerifier.create(clientResourceMono).assertNext(r -> {
            assertThat(r).isNull();
        }).expectComplete().verify();
    }
    
}