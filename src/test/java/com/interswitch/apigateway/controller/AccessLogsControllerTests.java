package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccessLogsControllerTests {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    MongoAccessLogsRepository mongoAccessLogsRepository;

    @Autowired
    AccessLogsController accessLogsController;
    private AccessLogs accessLogs;
    private String accessToken;

    @BeforeEach
    public void setup() throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .notBeforeTime(new Date())
                .audience("api-gateway")
                .claim("user_name", "adesegun.adeyemo")
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader,payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = "Bearer " + jws.serialize();

        accessLogs = new AccessLogs();
        accessLogs.setId("accessLogs1");
        accessLogs.setAction(AccessLogs.Action.CREATE);
        accessLogs.setEntity(AccessLogs.Entity.PRODUCT);
        accessLogs.setEntityId("productId");
        accessLogs.setApi("/products");
        accessLogs.setTimestamp(LocalDateTime.now());
        accessLogs.setUsername("user.name");
        accessLogs.setStatus(AccessLogs.Status.SUCCESSFUL);
    }

    @Test
    public void testGetPagedDefaultValues(){
        this.webClient.get()
                .uri("/audit")
                .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(AccessLogs.class).contains(accessLogs);
    }

    @Test
    public void testGetPaged(){
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit")
                        .queryParam("pageNum", "40")
                        .queryParam("pageSize", "20")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(AccessLogs.class).contains(accessLogs);
    }

    @Test
    public void testGetSearchValue(){
        this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit/search")
                        .queryParam("pageNum", "0")
                        .queryParam("pageSize", "30")
                        .queryParam("searchValue", "adebola.owolabi")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(AccessLogs.class).contains(accessLogs);
    }
}
