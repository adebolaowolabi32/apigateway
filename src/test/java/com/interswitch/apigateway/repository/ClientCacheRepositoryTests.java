package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.config.CacheConfig;
import com.interswitch.apigateway.model.Client;
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

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataRedisTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {CacheConfig.class, ClientMongoRepository.class, ClientCacheRepository.class})
public class ClientCacheRepositoryTests {

    @Autowired
    private ClientCacheRepository clientCacheRepository;

    @MockBean
    private ClientMongoRepository clientMongoRepository;

    private List resourceIds;
    private List origins;
    private Client client;
    private String  clientId = "testclient";


    @BeforeEach
    public void setUp() {
        resourceIds = Arrays.asList("passport/oauth/token", "passport/oauth/authorize");
        origins = Arrays.asList("https://qa.interswitchng.com", "http://localhost:3000");
        client = new Client("id", clientId, Client.Status.APPROVED, origins, resourceIds);
        clientCacheRepository.save(client).block();
    }

    @AfterEach
    public void delete(){
        clientCacheRepository.deleteByClientId(client.getClientId()).block();
    }

    @Test
    public void testGetClients() {
        StepVerifier.create(clientCacheRepository.findAll()).expectNextCount(1);
    }

    @Test
    public void testFindClient() {
        Mono<Client> clientMono = clientCacheRepository.findByClientId(client.getClientId());

        StepVerifier.create(clientMono).assertNext(r -> {
            assertThat(r.getId()).isEqualTo("id");
            assertThat(r.getClientId()).isEqualTo(clientId);
            assertThat(r.getOrigins()).hasSize(2);
            assertThat(r.getResourceIds()).hasSize(2);
        }).expectComplete().verify();
    }

    @Test
    public void testUpdateClient() {
        resourceIds = Arrays.asList("passport/oauth/authorize");
        origins = Arrays.asList("https://qa.interswitchng.com", "http://api-gateway-ui.com", "http://localhost:3000");
        client.setOrigins(origins);
        client.setResourceIds(resourceIds);
        clientCacheRepository.update(client).block();
        Mono<Client> clientMono = clientCacheRepository.findByClientId(client.getClientId());

        StepVerifier.create(clientMono).assertNext(r -> {
            assertThat(r.getOrigins()).hasSize(3);
            assertThat(r.getResourceIds()).hasSize(1);
        }).expectComplete().verify();
    }
}