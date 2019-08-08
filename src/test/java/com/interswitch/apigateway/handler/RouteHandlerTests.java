package com.interswitch.apigateway.handler;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.repository.MongoEnvironmentRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class RouteHandlerTests {

    Env env = new Env();
    ServerWebExchange exchange;
    @MockBean
    private MongoEnvironmentRepository repository;

    private RouteHandlerMapping mapping;

    private HttpHeaders headers = new HttpHeaders();
    private String accessToken;
    private Route route;

    @BeforeEach
    public void setup() throws JOSEException {
        env.setId("testRoute");
        env.setRouteId("testRoute");
        env.setUat("https://twitter.com");
        env.setSandbox("https://google.com");
    }

    @Test
    public void testRouteToUat() throws JOSEException {
        accessToken = getAccessToken("uat");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/")
                .header("Authorization", "Bearer " + accessToken)
                .build();
        exchange = MockServerWebExchange.from(request);
        route = Route.async().id("testRoute").uri("https://twitter.com")
                .predicate(swe -> true).build();
        RouteLocator routeLocator = () -> Flux.just(route)
                .hide();
        when(repository.findByRouteId(env.getRouteId())).thenReturn(Mono.just(env));
        when(repository.save(env)).thenReturn(Mono.just(env));
        mapping = new RouteHandlerMapping(null, routeLocator, new GlobalCorsProperties(), new MockEnvironment(), repository);
        StepVerifier.create(mapping.lookupRoute(exchange)).expectNext(route).expectComplete().verify();
    }

    @Test
    public void testRouteToSandbox() throws JOSEException {
        accessToken = getAccessToken("test");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/")
                .header("Authorization", "Bearer " + accessToken)
                .build();
        exchange = MockServerWebExchange.from(request);
        route = Route.async().id("testRoute").uri("https://google.com")
                .predicate(swe -> true).build();
        RouteLocator routeLocator = () -> Flux.just(route)
                .hide();
        when(repository.findByRouteId(env.getRouteId())).thenReturn(Mono.just(env));
        when(repository.save(env)).thenReturn(Mono.just(env));
        mapping = new RouteHandlerMapping(null, routeLocator, new GlobalCorsProperties(), new MockEnvironment(), repository);
        StepVerifier.create(mapping.lookupRoute(exchange)).expectNext(route).expectComplete().verify();
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
