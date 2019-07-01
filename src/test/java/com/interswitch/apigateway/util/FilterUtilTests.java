package com.interswitch.apigateway.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {FilterUtil.class})
public class FilterUtilTests {
    @Autowired
    FilterUtil filterUtil;
    JWTClaimsSet claims;
    JWSHeader jwsHeader;
    String accessToken;
    private ServerWebExchange exchange;

    public void setup(String aud) throws JOSEException, ParseException {
        claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .notBeforeTime(new Date())
                .claim("env", "TEST")
                .claim("client_id", "testClient")
                .audience(aud)
                .jwtID(UUID.randomUUID().toString())
                .build();

        jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = jws.serialize();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/path")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    public void testDecodeBearerToken() throws ParseException, JOSEException {
        this.setup("api-gateway");
        assertThat(filterUtil.decodeBearerToken(exchange.getRequest().getHeaders()).serialize()).isEqualTo(accessToken);
    }

    @Test
    public void testGetAudienceFromBearerToken() throws ParseException, JOSEException {
        this.setup("api-gateway");
        JWT accessToken = filterUtil.decodeBearerToken(exchange.getRequest().getHeaders());
        assertThat(filterUtil.getAudienceFromBearerToken(accessToken)).isEqualTo(Arrays.asList("api-gateway"));
    }

    @Test
    public void testGetEnvironmentFromBearerToken() throws ParseException, JOSEException {
        this.setup("api-gateway");
        JWT accessToken = filterUtil.decodeBearerToken(exchange.getRequest().getHeaders());
        assertThat(filterUtil.getEnvironmentFromBearerToken(accessToken)).isEqualTo("TEST");

    }

    @Test
    public void testGetClientIdFromBearerToken() throws ParseException, JOSEException {
        this.setup("api-gateway");
        JWT accessToken = filterUtil.decodeBearerToken(exchange.getRequest().getHeaders());
        assertThat(filterUtil.getClientIdFromBearerToken(accessToken)).isEqualTo("testClient");
    }

}
