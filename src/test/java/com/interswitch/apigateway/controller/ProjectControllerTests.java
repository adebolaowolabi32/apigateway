package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.*;
import com.interswitch.apigateway.repository.MongoProjectRepository;
import com.interswitch.apigateway.repository.MongoResourceRepository;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {ProjectController.class})
public class ProjectControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private PassportService passportService;

    @MockBean
    private MongoProjectRepository mongoProjectRepository;

    @MockBean
    private MongoResourceRepository mongoResourceRepository;

    private ArgumentCaptor<PassportClient> captor = ArgumentCaptor.forClass(PassportClient.class);
    private ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<Env> captor2 = ArgumentCaptor.forClass(Env.class);


    private String accessToken;

    private Project project;

    private Resource resource;

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
        project.setId("testprojectone");
        project.setName("testprojectname");
        project.setType(Project.Type.web);
        project.setDescription("test project description");
        project.setAuthorizedGrantTypes(Set.of(GrantType.authorization_code));
        project.setRegisteredRedirectUris(Collections.emptySet());
        project.setLogoUrl("");
        project.setOwner("project.owner");
        project.setClientId("testClientId", Env.TEST);
        project.setClientId("liveClientId", Env.LIVE);

        resource = new Resource();
        resource.setId("testresourceone");
        resource.setName("testresourcename");
        resource.setMethod(HttpMethod.GET);
        resource.setPath("/path");
        resource.setProduct(new Product());
    }

    @Test
    public void testGetAll() {
        when(mongoProjectRepository.findByOwner(project.getOwner())).thenReturn(Flux.fromIterable(Collections.singletonList(project)));
        this.webClient.get()
                .uri("/projects")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Project.class);
    }

    @Test
    public void testFindById() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        this.webClient.get()
                .uri("/projects/{projectId}", project.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Project.class);
    }

    @Test
    public void testCreate() {
        when(this.mongoProjectRepository.existsByName(project.getName())).thenReturn(Mono.just(false));
        when(mongoProjectRepository.save(any(Project.class))).thenReturn(Mono.just(project));
        when(passportService.createPassportClient(any(PassportClient.class), any(String.class), any(Env.class))).thenReturn(Mono.just(new PassportClient()));
        this.webClient.post()
                .uri("/projects")
                .body(BodyInserters.fromObject(project))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Project.class);

    }

    @Test
    public void testUpdate() {
        when(this.mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoProjectRepository.save(any(Project.class))).thenReturn(Mono.just(project));
        when(passportService.updatePassportClient(any(PassportClient.class), any(String.class), any(Env.class))).thenReturn(Mono.empty());
        this.webClient.put()
                .uri("/projects")
                .body(BodyInserters.fromObject(project))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Project.class);

    }

    @Test
    public void testGetCredentials() {
        when(mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(passportService.getPassportClient(any(String.class), any(String.class), any(Env.class))).thenReturn(Mono.just(new PassportClient()));
        this.webClient.get()
                .uri("/projects/{projectId}/credentials/{env}", project.getId(), Env.TEST)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Client.class);
    }

    @Test
    public void testGetRequestedResources() {
        when(this.mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        this.webClient.get()
                .uri("/projects/{projectId}/requested", project.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map.class);

    }

    @Test
    public void testSaveRequestedResources() {
        Map<String, Object> resources = Map.of("resources", Arrays.asList("resourceone", "resourcetwo"));
        when(this.mongoProjectRepository.findById(project.getId())).thenReturn(Mono.just(project));
        when(mongoResourceRepository.findById(any(String.class))).thenReturn(Mono.just(resource));
        when(mongoProjectRepository.save(project)).thenReturn(Mono.just(project));
        when(passportService.updatePassportClient(captor.capture(), captor1.capture(), captor2.capture())).thenReturn(Mono.empty());
        this.webClient.post()
                .uri("/projects/{projectId}/requested", project.getId())
                .body(BodyInserters.fromObject(resources))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(Project.class);

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
