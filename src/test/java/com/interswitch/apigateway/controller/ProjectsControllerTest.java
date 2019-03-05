package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Projects;
import com.interswitch.apigateway.repository.MongoProjectsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
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

import java.net.URISyntaxException;
import java.util.Collections;

import static org.mockito.BDDMockito.when;

@ActiveProfiles("dev")
@WebFluxTest(value = {ProjectsController.class}, excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveManagementWebSecurityAutoConfiguration.class,
        ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {MongoProjectsRepository.class, ProjectsController.class})
public class ProjectsControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoProjectsRepository mongo;

    private Projects project = new Projects();

    @BeforeEach
    public void setup() throws URISyntaxException {
        project = new Projects("id","projectName","passportId","tester@gmail.com","testappSecret","testappId");
    }
    @Test
    public void testGetProjects(){
        when(mongo.findAll()).thenReturn(Flux.just(project));
        this.webClient.get()
                .uri("/projects")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Projects.class);
    }

    @Test
    public void testSaveProjects(){
        when(mongo.save(project)).thenReturn(Mono.just(project));
        this.webClient.post()
                .uri("/projects/save")
                .body(BodyInserters.fromObject(project))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Projects.class);
    }

    @Test
    public void findByAppId(){
        when(mongo.findByAppId(project.getAppId())).thenReturn(Mono.just(project));
        this.webClient.get()
                .uri("/projects/{appId}", Collections.singletonMap("appId",project.getAppId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> Assertions.assertThat(response.getResponseBody()).isNotNull());
    }

    @Test
    public void testUpdateProjects(){
        when(this.mongo.findByAppId(project.getAppId())).thenReturn(Mono.just(project));
        when(this. mongo.save(project)).thenReturn(Mono.just(project));
        this.webClient.put()
                .uri("/projects/update")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromObject(project))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Projects.class);
    }
    @Test
    public void testDeleteProjects(){
        when(mongo.deleteById(project.getId())).thenReturn(Mono.empty());
        when(mongo.findById(project.getId())).thenReturn(Mono.just(project));
        this.webClient.delete()
                .uri("/projects/delete/{id}",  Collections.singletonMap("id",project.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}
