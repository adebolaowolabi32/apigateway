/*
package com.interswitch.apigateway;

import com.interswitch.apigateway.config.UserConfig;
import com.interswitch.apigateway.controller.ClientController;
import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoClientRepository;
import com.interswitch.apigateway.repository.MongoUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("dev")
@Import(UserConfig.class)
//@ImportAutoConfiguration(classes = {UserConfig.class, MongoUserRepository.class, MongoClientRepository.class})
//@WebFluxTest
///@WebFluxTest( excludeAutoConfiguration = {SecurityAutoConfiguration.class, ReactiveSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
@SpringBootTest(classes = ApiGatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = ReactiveSecurityAutoConfiguration.class)
public class SystemAdminUserTests {
    @Autowired
    private WebTestClient webClient;

    @Test
    public void testForSystemAdminUser() {
        if (this.webClient.get().uri("/users/{username}", "systemAdministrator")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isUnauthorized()
                //.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody() != null) {
        System.out.println(webClien);
        }
               */
/* .jsonPath("$.id").isEqualTo("systemAdmin")
                .jsonPath("$.role").isEqualTo(User.Role.ADMIN);*//*

    }
}
*/
