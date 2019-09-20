package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.*;
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
@ContextConfiguration(classes = {ProjectController.class})
public class ProjectControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ProjectService projectService;

    private String accessToken;

    private ProjectData projectData;

    private String projectOwner = "project.owner";

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
        projectData = new ProjectData();
        projectData.setId("testprojectone");
        projectData.setName("testprojectname");
        projectData.setType(Project.Type.web);
        projectData.setDescription("test project description");
        projectData.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code));
        projectData.setRegisteredRedirectUris(Collections.emptySet());
        projectData.setLogoUrl("");
        projectData.setOwner(projectOwner);
        projectData.setClients(Map.of(Env.TEST, "testClientId", Env.LIVE, "liveClientId"));
    }

    @Test
    public void testGetAll() {
        when(projectService.getAllProjects(projectOwner)).thenReturn(Flux.fromIterable(Collections.singleton(projectData)));
        this.webClient.get()
                .uri("/projects")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(ProjectData.class);
    }

    @Test
    public void testFindById() {
        when(projectService.getProject(projectOwner, projectData.getId())).thenReturn(Mono.just(projectData));
        this.webClient.get()
                .uri("/projects/{projectId}", projectData.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectData.class);
    }

    @Test
    public void testCreate() {
        when(projectService.createProject(projectOwner, projectData)).thenReturn(Mono.just(projectData));
        this.webClient.post()
                .uri("/projects")
                .body(BodyInserters.fromObject(projectData))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProjectData.class);
    }

    @Test
    public void testUpdate() {
        when(projectService.updateProject(projectOwner, projectData)).thenReturn(Mono.just(projectData));
        this.webClient.put()
                .uri("/projects")
                .body(BodyInserters.fromObject(projectData))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectData.class);
    }

    @Test
    public void testGetCredentials() {
        when(projectService.getClientCredentials(projectOwner, projectData.getId(), Env.TEST)).thenReturn(Mono.just(new Client()));
        this.webClient.get()
                .uri("/projects/{projectId}/credentials/{env}", projectData.getId(), Env.TEST)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class);
    }

    @Test
    public void testGetRequestedResources() {
        when(projectService.getRequestedResources(projectOwner, projectData.getId())).thenReturn(Flux.fromIterable(Collections.singleton(new ProductRequest())));
        this.webClient.get()
                .uri("/projects/{projectId}/requested", projectData.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductRequest.class);
    }

    @Test
    public void testSaveRequestedResources() {
        Map<String, LinkedHashSet<String>> resources = Map.of("resources", new LinkedHashSet(Arrays.asList("resourceone", "resourcetwo")));
        when(projectService.saveRequestedResources(projectOwner, projectData.getId(), resources)).thenReturn(Mono.empty());
        this.webClient.post()
                .uri("/projects/{projectId}/requested", projectData.getId())
                .body(BodyInserters.fromObject(resources))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Void.class);
    }

    @Test
    public void testGetApprovedResources() {
        when(projectService.getApprovedResources(projectOwner, projectData.getId())).thenReturn(Flux.fromIterable(Collections.singleton(new ProductRequest())));
        this.webClient.get()
                .uri("/projects/{projectId}/approved", projectData.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductRequest.class);
    }

    /*@Test
    public void testAssignProduct(){
        Product p = new Product();
        p.setId("testProductId");
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(mongoProductRepository.findById(p.getId())).thenReturn(Mono.just(p));
        when(mongoProductRepository.save(p)).thenReturn(Mono.just(p));
        this.webClient.post()
                .uri("/projects/{projectId}/products/{productId}", project.getId(), p.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Project.class);
    }

    @Test
    public void testUnassignProduct(){
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(mongoProductRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(mongoProductRepository.save(product)).thenReturn(Mono.just(product));
        this.webClient.delete()
                .uri("/projects/{projectId}/products/{productId}", project.getId(), product.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Project.class);
    }

    @Test
    public void testGetAssignedProducts(){
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        this.webClient.get()
                .uri("/projects/{projectId}/products", project.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class);
    }*/
}
