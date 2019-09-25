package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Project;
import com.interswitch.apigateway.service.ProjectService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {GoliveController.class})
public class GoliveControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ProjectService projectService;

    private String accessToken;

    private String projectOwner = "project.owner";

    private String projectId = "projectId";

    private Map<String, LinkedHashSet<String>> resources = Map.of("resources", new LinkedHashSet(Arrays.asList("resourceone", "resourcetwo")));

    @BeforeEach
    public void setup() throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .notBeforeTime(new Date())
                .claim("user_name", projectOwner)
                .jwtID(UUID.randomUUID().toString())
                .build();
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));

        accessToken = jws.serialize();
    }

    @Test
    public void testGoliveRequest() {
        when(projectService.requestProjectGoLive(projectOwner, projectId)).thenReturn(Mono.empty());
        this.webClient.post()
                .uri("/golive/request/{projectId}", projectId)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Void.class);
    }

    @Test
    public void testGoliveApproval() {
        when(projectService.saveApprovedResources(projectId, resources)).thenReturn(Mono.empty());
        this.webClient.post()
                .uri("/golive/approve/{projectId}", projectId)
                .body(BodyInserters.fromObject(resources))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Void.class);
    }

    @Test
    public void testGoliveDecline() {
        when(projectService.declineRequestedResources(projectId, resources)).thenReturn(Mono.empty());
        this.webClient.post()
                .uri("/golive/decline/{projectId}", projectId)
                .body(BodyInserters.fromObject(resources))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Void.class);
    }

    @Test
    public void testGetPendingProjects() {
        when(projectService.getPendingProjects()).thenReturn(Flux.just(new Project()));
        this.webClient.get()
                .uri("/golive/pending")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Project.class);
    }
}
