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
    private FilterUtil filterUtil;
    private HttpHeaders headers = new HttpHeaders();
    private JWT token;
    private String anyStringClaim = "sampleClaimValue";
    private List<String> anyListClaim = Arrays.asList("item-^/one", "item-$/two");
    private List<String> audience = Arrays.asList("audienceOne", "audienceTwo");
    private String accessToken;

    @BeforeEach
    public void setup() throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .notBeforeTime(new Date())
                .claim("any_string_claim", anyStringClaim)
                .claim("client_id", anyStringClaim)
                .claim("any_list_claim", anyListClaim)
                .audience(audience)
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = jws.serialize();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        token = FilterUtil.decodeBearerToken(headers);
    }

    @Test
    public void testDecodeBearerToken() {
        assertThat(FilterUtil.decodeBearerToken(headers).serialize()).isEqualTo(accessToken);
    }

    @Test
    public void testGetAudienceFromBearerToken() {
        assertThat(FilterUtil.getClaimAsListFromBearerToken(token, "aud")).isEqualTo(audience);
    }

    @Test
    public void getClientIdClaimAsStringFromBearerToken() {
        assertThat(FilterUtil.getClaimAsStringFromBearerToken(token, "client_id")).isEqualTo(anyStringClaim);
    }

    @Test
    public void getClaimAsStringFromBearerToken() {
        assertThat(FilterUtil.getClaimAsStringFromBearerToken(token, "any_string_claim")).isEqualTo(anyStringClaim.toLowerCase());

    }

    @Test
    public void getClaimAsListFromBearerToken() {
        assertThat(FilterUtil.getClaimAsListFromBearerToken(token, "any_list_claim")).isEqualTo(anyListClaim);
    }
}
