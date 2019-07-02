package com.interswitch.apigateway.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {FilterUtil.class})
public class FilterUtilTests {
    @Autowired
    FilterUtil filterUtil;
    HttpHeaders headers = new HttpHeaders();
    JWT token;
    String client_id = "testClient";
    String environment = "TEST";
    String username = "username";
    List<String> audience = Arrays.asList("api-gateway");
    String accessToken;

    @BeforeEach
    public void setup() throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .notBeforeTime(new Date())
                .claim("env", environment)
                .claim("client_id", client_id)
                .claim("user_name", username)
                .audience(audience)
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = jws.serialize();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        token = filterUtil.decodeBearerToken(headers);
    }

    @Test
    public void testDecodeBearerToken() {
        assertThat(filterUtil.decodeBearerToken(headers).serialize()).isEqualTo(accessToken);
    }

    @Test
    public void testGetAudienceFromBearerToken() {
        assertThat(filterUtil.getAudienceFromBearerToken(token)).isEqualTo(audience);
    }

    @Test
    public void testGetEnvironmentFromBearerToken() {
        assertThat(filterUtil.getEnvironmentFromBearerToken(token)).isEqualTo(environment);

    }

    @Test
    public void testGetClientIdFromBearerToken() {
        assertThat(filterUtil.getClientIdFromBearerToken(token)).isEqualTo(client_id);
    }

    @Test
    public void testGetUsernameFromBearerToken() {
        assertThat(filterUtil.getUsernameFromBearerToken(token)).isEqualTo(username);
    }

}
