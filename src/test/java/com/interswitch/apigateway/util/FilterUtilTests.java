package com.interswitch.apigateway.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
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
import java.util.Date;
import java.util.UUID;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {FilterUtil.class})
public class FilterUtilTests {
    @Autowired
    FilterUtil filterUtil;
    JWTClaimsSet claims;
    JWSHeader jwsHeader;
    String accessToken;
    HttpHeaders headers;
    private ServerWebExchange exchange;

    public void setup(String aud) throws JOSEException, ParseException {
        claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .notBeforeTime(new Date())
                .audience(aud)
                .jwtID(UUID.randomUUID().toString())
                .build();

        jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = jws.serialize();
        headers.add("Autorization", "Bearer " + accessToken);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/path")
                .header("Authorization", accessToken)
                .build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    public void testDecodeBearerToken() {

    }


}
