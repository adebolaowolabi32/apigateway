package com.interswitch.apigateway.handler;

import com.interswitch.apigateway.model.Environment;
import com.interswitch.apigateway.repository.MongoEnvironmentRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.UUID;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class RouteHandlerTests {

    Environment environment = new Environment();
    ServerWebExchange exchange;
    @MockBean
    private MongoEnvironmentRepository repository;
    @Autowired
    private RouteHandlerMapping handlerMapping;
    private HttpHeaders headers = new HttpHeaders();
    private String accessToken;

    @BeforeEach
    public void setup() throws JOSEException {
        environment.setId("testRoute");
        environment.setRouteId("testRoute");
        environment.setUat("https://twitter.com");
        environment.setSandbox("https://google.com");

    }

    @Test
    public void testRouteToUat() throws JOSEException {
        accessToken = getAccessToken("uat");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/")
                .header("Authorization", accessToken)
                .build();
        exchange = MockServerWebExchange.from(request);
        when(repository.findByRouteId(environment.getRouteId())).thenReturn(Mono.just(environment));
        when(repository.save(environment)).thenReturn(Mono.just(environment));
        when(decodeBearerToken(any())).thenCallRealMethod();
        when(getClaimAsStringFromBearerToken(any(), any())).thenCallRealMethod();
        StepVerifier.create(handlerMapping.lookupRoute(exchange)).expectComplete().verify();
        assertThat(handlerMapping.lookupRoute(exchange).block().getUri()).isEqualTo(environment.getSandbox());

    }

    @Test
    public void testRouteToSandbox() throws JOSEException {
        accessToken = getAccessToken("test");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/")
                .header("Authorization", "Bearer " + accessToken)
                .build();
        exchange = MockServerWebExchange.from(request);
        when(repository.findByRouteId(environment.getRouteId())).thenReturn(Mono.just(environment));
        when(repository.save(environment)).thenReturn(Mono.just(environment));
        StepVerifier.create(handlerMapping.lookupRoute(exchange)).expectComplete().verify();
        assertThat(handlerMapping.lookupRoute(exchange).block().getUri()).isEqualTo(environment.getSandbox());
    }

    public String getAccessToken(String env) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .notBeforeTime(new Date())
                .claim("env", env)
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = jws.serialize();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        return accessToken;
    }
}
