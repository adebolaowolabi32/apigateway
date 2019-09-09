package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.util.FilterUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@DataRedisTest
@ActiveProfiles("dev")
@EnableAutoConfiguration
@ContextConfiguration(classes = {AccessControlFilter.class, FilterUtil.class})
public class AccessControlFilterTests {

    @Autowired
    private AccessControlFilter filter;

    @MockBean
    private GatewayFilterChain filterChain;

    private ServerWebExchange exchange;

    public void setup(String env, String routeId, List<String> resources) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .notBeforeTime(new Date())
                .audience("isw-core")
                .claim("env", env)
                .claim("api_resources", resources)
                .jwtID(UUID.randomUUID().toString())
                .build();
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader,payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        String accessToken = "Bearer " + jws.serialize();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/path/help")
                .header("Authorization",accessToken)
                .build();
        exchange = MockServerWebExchange.from(request);

        Route route = Route.async().id(routeId).uri(request.getURI()).order(0)
                .predicate(swe -> true).build();

        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, route);
    }

    @Test
    public void allRequestsWithEnvironmentTestClaimShouldPass() throws JOSEException {
        this.setup("TEST", "id", Collections.emptyList());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allRequestsFromPassportShouldPass() throws JOSEException {
        this.setup(null, "passport", Collections.emptyList());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allOptionsRequestsShouldPass() {
        ServerWebExchange exchange = MockServerWebExchange.from( MockServerHttpRequest.options("http://localhost:8080/path").build());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void liveRequestsWithoutResourcePermissionsShouldFail() throws JOSEException {
        this.setup("LIVE","id", Collections.emptyList());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectError().verify();
    }

    @Test
    public void liveRequestsWithWrongPatternMatchingShouldFail() throws JOSEException {
        this.setup("LIVE", "id", Collections.singletonList("GET/*?/(path}[]&^$|"));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectError().verify();
    }
    @Test
    public void liveRequestsWithProperResourcePermissionsShouldPass() throws JOSEException {
        this.setup("LIVE", "id", Collections.singletonList("GET/pat?/*"));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }
}
