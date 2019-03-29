package com.interswitch.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;


@ActiveProfiles("dev")
@WebFluxTest
public class AccessCheckTests {
    @Autowired
    private WebTestClient webClient;

    @Test
    public void testAccess(){
        this.webClient.get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/vnd.spring-boot.actuator.v2+json;charset=UTF-8");
    }
}
