package com.interswitch.apigateway.handler;

import com.interswitch.apigateway.model.RouteEnvironment;
import com.interswitch.apigateway.repository.MongoRouteEnvironmentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class RouteHandlerTests {

    RouteEnvironment routeEnvironment = new RouteEnvironment();
    ServerWebExchange exchange;

    @MockBean
    private MongoRouteEnvironmentRepository repository;

    private RouteHandlerMapping mapping;
    private Route route;
    private RouteLocator routeLocator;
    private String testUrl = "https://sampletestservice.com:443";
    private String liveUrl = "https://sampleliveservice.com:443";

    @BeforeEach
    public void setup() {
        route = Route.async().id("sampleRoute").uri(liveUrl)
                .predicate(swe -> true).build();
        routeLocator = () -> Flux.just(route).hide();
        mapping = new RouteHandlerMapping(null, routeLocator, new GlobalCorsProperties(), new MockEnvironment(), repository);

        routeEnvironment.setId("sampleRoute");
        routeEnvironment.setRouteId("sampleRoute");
        routeEnvironment.setTestURL(testUrl);

        when(repository.findByRouteId(routeEnvironment.getRouteId())).thenReturn(Mono.just(routeEnvironment));
        when(repository.save(routeEnvironment)).thenReturn(Mono.just(routeEnvironment));
    }

    @Test
    public void testRouteToTestURL() throws JOSEException {
        exchange = getExchange("test");
        StepVerifier.create(mapping.lookupRoute(exchange)).assertNext(route -> {
            assertThat(route.getUri().toString()).isEqualTo(testUrl);
        }).expectComplete().verify();
    }

    @Test
    public void testRouteToLiveURL() throws JOSEException {
        exchange = getExchange("live");
        StepVerifier.create(mapping.lookupRoute(exchange)).assertNext(route -> {
            assertThat(route.getUri().toString()).isEqualTo(liveUrl);
        }).expectComplete().verify();
    }

    public ServerWebExchange getExchange(String env) throws JOSEException {
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
        String accessToken = jws.serialize();
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/")
                .header("Authorization", "Bearer " + accessToken)
                .build();
        exchange = MockServerWebExchange.from(request);
        return exchange;
    }
}
