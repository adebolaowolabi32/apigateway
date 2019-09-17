package com.interswitch.apigateway.service;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.GrantType;
import com.interswitch.apigateway.model.PassportClient;
import com.interswitch.apigateway.model.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

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
        testCreatePassportClient(Env.TEST);
        testUpdatePassportClient(Env.TEST);

    }

    @Test
    private void testCreatePassportClient(Env env) {
        passportService.createPassportClient(passportClient, env)
                .doOnSuccess(createdClient -> {
                    project.setClientId(createdClient.getClientId(), env);
                }).block();
        clientId = project.getClientId(env);

        StepVerifier.create(passportService.getPassportClient(clientId, env)).assertNext(passportClient1 -> {
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
    private void testUpdatePassportClient(Env env) {
        passportClient.setClientId(clientId);
        passportClient.setDescription("newDescription");
        passportClient.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code, GrantType.refresh_token, GrantType.client_credentials));
        passportService.updatePassportClient(passportClient, env).block();

        StepVerifier.create(passportService.getPassportClient(clientId, env)).assertNext(updatedClient -> {
            assertThat(updatedClient.getDescription()).isEqualTo(passportClient.getDescription());
            assertThat(updatedClient.getAuthorizedGrantTypes()).isEqualTo(passportClient.getAuthorizedGrantTypes());
        }).expectComplete().verify();
    }
}
