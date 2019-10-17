package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
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

import java.util.Collections;

import static org.mockito.Mockito.when;

@ActiveProfiles("dev")
@WebFluxTest(excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@ContextConfiguration(classes = {UserController.class})
public class UserControllerTests {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private MongoUserRepository mongoUserRepository;

    private User user;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId("test_user_id");
        user.setUsername("test_username@interswitch.com");
    }

    @Test
    public void testFindAll(){
        when(mongoUserRepository.findAll()).thenReturn(Flux.fromIterable(Collections.singletonList(user)));
        this.webClient.get()
                .uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(User.class);
    }

    @Test
    public void testRegister(){
        when(mongoUserRepository.findByUsername(user.getUsername())).thenReturn(Mono.empty());
        when(mongoUserRepository.save(user)).thenReturn(Mono.just(user));
        this.webClient.post()
                .uri("/users")
                .body(BodyInserters.fromObject(user))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class);
    }

    @Test
    public void testFindByUsername(){
        when(mongoUserRepository.findByUsername(user.getUsername())).thenReturn(Mono.just(user));
        this.webClient.get()
                .uri("/users/{username}", user.getUsername())
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class);
    }

    @Test
    public void testDelete(){
        when(mongoUserRepository.deleteById(user.getId())).thenReturn(Mono.empty());
        when(mongoUserRepository.findByUsername(user.getUsername())).thenReturn(Mono.just(user));
        this.webClient.delete()
                .uri("/users/{username}",  user.getUsername())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

}