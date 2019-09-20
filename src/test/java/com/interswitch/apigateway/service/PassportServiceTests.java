package com.interswitch.apigateway.service;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.PassportClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class PassportServiceTests {
    @Autowired
    private PassportService passportService;

    private PassportClient passportClient;

    private String clientId;

    @Test
    public void testPassportService() {
        passportClient = new PassportClient();
        passportClient.setClientId("clientId");
        passportClient.setClientName("Test Client " + UUID.randomUUID().toString());
        passportClient.setDescription("description");
        passportClient.setScope(Set.of("profile"));
        passportClient.setClientSecret("secret");
        passportClient.setLogoUrl("https://logourl");
        passportClient.setResourceIds(Set.of("api-gateway", "passport"));
        passportClient.setAuthorizedGrantTypes(Set.of("authorization_code", "client_credentials"));
        passportClient.setRegisteredRedirectUri(Set.of("http://redirectUrl"));
        passportClient.setClientOwner("api.gateway@interswitch.com");
        passportClient.setAutoApproveScopes(Collections.emptySet());
        passportClient.setAuthorities(Collections.emptyList());
        passportClient.setAdditionalInformation(Map.of("env", Env.TEST));
        passportClient.setAccessTokenValiditySeconds(1800);
        passportClient.setRefreshTokenValiditySeconds(1209600);

        testCreatePassportClient(Env.TEST);
        testUpdatePassportClient(Env.TEST);
        testGetAllPassportClients(Env.TEST);
    }

    private void testCreatePassportClient(Env env) {
        passportService.createPassportClient(passportClient, env)
                .doOnSuccess(createdClient -> {
                    clientId = createdClient.getClientId();
                }).block();

        StepVerifier.create(passportService.getPassportClient(clientId, env)).assertNext(retrievedClient -> {
            assertThat(retrievedClient.getClientId()).isEqualTo(clientId);
            assertThat(retrievedClient.getClientName()).isEqualTo(passportClient.getClientName());
            assertThat(retrievedClient.getClientSecret()).isEqualTo(passportClient.getClientSecret());
            assertThat(retrievedClient.getClientOwner()).isEqualTo(passportClient.getClientOwner());
            assertThat(retrievedClient.getDescription()).isEqualTo(passportClient.getDescription());
            assertThat(retrievedClient.getScope()).isEqualTo(passportClient.getScope());
            assertThat(retrievedClient.getLogoUrl()).isEqualTo(passportClient.getLogoUrl());
            assertThat(retrievedClient.getResourceIds()).isEqualTo(passportClient.getResourceIds());
            assertThat(retrievedClient.getAuthorizedGrantTypes()).isEqualTo(passportClient.getAuthorizedGrantTypes());
            assertThat(retrievedClient.getRegisteredRedirectUri()).isEqualTo(passportClient.getRegisteredRedirectUri());
            assertThat(retrievedClient.getAuthorities()).isEqualTo(passportClient.getAuthorities());
            assertThat(retrievedClient.getAutoApproveScopes()).isEqualTo(passportClient.getAutoApproveScopes());
            assertThat(retrievedClient.getAccessTokenValiditySeconds()).isEqualTo(passportClient.getAccessTokenValiditySeconds());
            assertThat(retrievedClient.getRefreshTokenValiditySeconds()).isEqualTo(passportClient.getRefreshTokenValiditySeconds());
        }).expectComplete().verify();
    }

    private void testUpdatePassportClient(Env env) {
        passportClient.setClientId(clientId);
        passportClient.setDescription("newDescription");
        passportClient.setAuthorizedGrantTypes(Set.of("authorization_code", "refresh_token", "client_credentials"));
        passportService.updatePassportClient(passportClient, env).block();

        StepVerifier.create(passportService.getPassportClient(clientId, env)).assertNext(updatedClient -> {
            assertThat(updatedClient.getDescription()).isEqualTo(passportClient.getDescription());
            assertThat(updatedClient.getAuthorizedGrantTypes()).isEqualTo(passportClient.getAuthorizedGrantTypes());
        }).expectComplete().verify();
    }

    private void testGetAllPassportClients(Env env) {
        StepVerifier.create(passportService.getPassportClients("api.gateway@interswitch.com", env)).expectNextCount(1);
    }
}
