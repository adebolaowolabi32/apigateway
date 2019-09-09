package com.interswitch.apigateway.service;

import com.interswitch.apigateway.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.interswitch.apigateway.service.PassportService.buildPassportClientForEnvironment;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class PassportServiceTests {
    @Autowired
    PassportService passportService;

    private PassportClient passportClient;

    private Project project;

    private String clientId;

    private String accessToken;// = "Bearer eyJhbGciOiJSUzI1NiJ9.eyJhdWQiOlsicGFzc3BvcnQiLCJwcm9qZWN0LXgtbWVyY2hhbnQiXSwic2NvcGUiOlsicHJvZmlsZSJdLCJqdGkiOiIwNTE3MDE2YS03YzhhLTQ3MjctOTBjZS00ZTY5ZTA0OGMyMDUiLCJjbGllbnRfaWQiOiJJS0lBQzQ0MjBEMTNBQkU1N0Q3NzhGQjcyNjNBMTdEMTNCNjBBOEFFNDEzNSJ9.RmhKED9bAuTyP7FCFbkgIDL--1ZDsQnqr_bPg7WWPZpYR6Wh-fnf6p1yRSP6PqItw78-08beo8XzPaFM96ZyLpfxwiEf3UPGOcLe1a_PHHQz7ajUfHznyTyNN6CJgVbGnZza-g7q-b1nmpLul-Ko_L5FV5gW0GPhN6HSWRF-pCr0RNbS6_lVFClp5t1iFlE66I4DB7k2ljCsSyuuW0o99Nj67wt3CzLXjTM0lX9FyAXYvUWwfJ1EExKCdsbJJPsQRcQtBIr0Z_V2Vr51d6wBp-0ytRXeua4qo7trZ2U2e_LThyxKgAQDEugKefrf1MdaJOJU3Np9DEXAFx7xU2yQ-g";


    private void getAccessToken(Env env) {
        MultiValueMap formData = new LinkedMultiValueMap();
        String client_id = "IKIAD9AFCEE45EBADD5EAA0D7FA2AA129B13B3F38DD1";
        String clientSecret = "secret";
        formData.setAll(Map.of("username", "api.gateway@interswitch.com", "password", "password", "grant_type", "password", "scope", "profile"));
        Client client = new Client();
        client.setClientId(client_id);
        client.setClientSecret(clientSecret);
        passportService.getAccessToken(formData, client, env).flatMap(result -> {
            Map<String, Object> json = (Map<String, Object>) result;
            this.accessToken = "Bearer " + json.get("access_token").toString();
            return Mono.empty();
        }).block();

    }

    @Test
    public void test() {
        project = new Project();
        project.setName("project name " + UUID.randomUUID().toString());
        project.setOwner("api.gateway@interswitch.com");
        project.setDescription("project description");
        project.setType(Project.Type.web);
        project.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code, GrantType.client_credentials));

        project.setRegisteredRedirectUris(Set.of("http://redirectUrl"));
        project.setLogoUrl("https://logourl");


        passportClient = buildPassportClientForEnvironment(project, Env.TEST);
        getAccessToken(Env.TEST);
        runTestToCreatePassportClient(Env.TEST);
        runTestToUpdatePassportClient(Env.TEST);

    }

    @Test
    private void runTestToCreatePassportClient(Env env) {
        passportService.createPassportClient(passportClient, accessToken, env)
                .doOnSuccess(createdClient -> {
                    project.setClientId(createdClient.getClientId(), env);
                }).block();
        clientId = project.getClientId(env);

        StepVerifier.create(passportService.getPassportClient(clientId, accessToken, env)).assertNext(passportClient1 -> {
            assertThat(passportClient1.getClientId()).isEqualTo(clientId);
            assertThat(passportClient1.getClientName()).isEqualTo(project.getName());
            assertThat(passportClient1.getClientOwner()).isEqualTo(project.getOwner());
            assertThat(passportClient1.getDescription()).isEqualTo(project.getDescription());
            assertThat(passportClient1.getLogoUrl()).isEqualTo(project.getLogoUrl());
            assertThat(passportClient1.getAuthorizedGrantTypes()).isEqualTo(project.getAuthorizedGrantTypes());
            assertThat(passportClient1.getRegisteredRedirectUri()).isEqualTo(project.getRegisteredRedirectUris());
            if (project.getType().equals(Project.Type.web)) {
                assertThat(passportClient1.getAccessTokenValiditySeconds()).isEqualTo(1800);
                assertThat(passportClient1.getRefreshTokenValiditySeconds()).isEqualTo(3600);
            }
            if (project.getType().equals(Project.Type.mobile)) {
                assertThat(passportClient1.getAccessTokenValiditySeconds()).isEqualTo(3600);
                assertThat(passportClient1.getRefreshTokenValiditySeconds()).isEqualTo(7200);
            }
            if (project.getType().equals(Project.Type.other)) {
                assertThat(passportClient1.getAccessTokenValiditySeconds()).isEqualTo(3600);
                assertThat(passportClient1.getRefreshTokenValiditySeconds()).isEqualTo(7200);
            }
        }).expectComplete().verify();
    }

    @Test
    private void runTestToUpdatePassportClient(Env env) {
        passportClient.setClientId(clientId);
        passportClient.setDescription("newDescription");
        passportClient.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code, GrantType.refresh_token, GrantType.client_credentials));
        passportService.updatePassportClient(passportClient, accessToken, env).block();

        StepVerifier.create(passportService.getPassportClient(clientId, accessToken, env)).assertNext(updatedClient -> {
            assertThat(updatedClient.getDescription()).isEqualTo(passportClient.getDescription());
            assertThat(updatedClient.getAuthorizedGrantTypes()).isEqualTo(passportClient.getAuthorizedGrantTypes());
        }).expectComplete().verify();
    }
}
