package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.PassportClient;
import com.interswitch.apigateway.model.Project;
import com.interswitch.apigateway.repository.MongoProductRepository;
import com.interswitch.apigateway.repository.MongoProjectRepository;
import com.interswitch.apigateway.service.PassportService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {GoliveController.class})
public class GoliveControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private PassportService passportService;

    @MockBean
    private MongoProjectRepository mongoProjectRepository;

    @MockBean
    private MongoProductRepository mongoProductRepository;

    private String accessToken;
    private Project project;
    private ArgumentCaptor<PassportClient> captor = ArgumentCaptor.forClass(PassportClient.class);
    private ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<Env> captor2 = ArgumentCaptor.forClass(Env.class);

    @BeforeEach
    public void setup() throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .notBeforeTime(new Date())
                .claim("user_name", "project.owner")
                .jwtID(UUID.randomUUID().toString())
                .build();
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = jws.serialize();

        project = new Project();
        project.setId("projectId");
        project.setName("projectName");
        project.setOwner("project.owner");
        project.setType(Project.Type.mobile);
    }

    @Test
    public void testGoliveRequest() {
        when(this.mongoProjectRepository.findById("projectId")).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(passportService.createPassportClient(captor.capture(), captor1.capture(), captor2.capture())).thenReturn(Mono.just(new PassportClient()));
        this.webClient.post()
                .uri("/golive/request/{projectId}", project.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Project.class);
    }

    @Test
    public void testGoliveApproval() {
        when(this.mongoProjectRepository.findById("projectId")).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(passportService.createPassportClient(captor.capture(), captor1.capture(), captor2.capture())).thenReturn(Mono.empty());
        this.webClient.post()
                .uri("/golive/approve/{projectId}", project.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Project.class);
    }
}
