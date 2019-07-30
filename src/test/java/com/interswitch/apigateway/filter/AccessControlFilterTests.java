package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.repository.MongoClientRepository;
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
@ContextConfiguration(classes = {MongoClientRepository.class, Client.class, AccessControlFilter.class, FilterUtil.class})
public class AccessControlFilterTests {

    @MockBean
    private MongoClientRepository mongoClientRepository;

    @Autowired
    private AccessControlFilter filter;

    @MockBean
    private GatewayFilterChain filterChain;

    private ServerWebExchange exchange;

    private Client client = new Client();

    private String clientId = "clientId";

    public void setup(String env, String routeId, List<String> resources) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .notBeforeTime(new Date())
                .audience("isw-core")
                .claim("env", env)
                .claim("client_id", clientId)
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
    public void liveRequestsWithNonExistentClientShouldFail() throws JOSEException {
        this.setup("LIVE","id", Collections.emptyList());
        when(mongoClientRepository.findByClientId(clientId)).thenReturn(Mono.empty());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectError().verify();
    }

    @Test
    public void liveRequestsWithoutResourcePermissionsShouldFail() throws JOSEException {
        this.setup("LIVE","id", Collections.emptyList());
        client.setId(clientId);
        client.setClientId(clientId);
        when(mongoClientRepository.findByClientId(clientId)).thenReturn(Mono.just(client));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectError().verify();
    }

    @Test
    public void liveRequestsWithWrongPatternMatchingShouldFail() throws JOSEException {
        this.setup("LIVE", "id", Collections.singletonList("GET/*?/(path}"));
        client.setId(clientId);
        client.setClientId(clientId);
        when(mongoClientRepository.findByClientId(clientId)).thenReturn(Mono.just(client));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectError().verify();
    }
    @Test
    public void liveRequestsWithProperClientAndResourcePermissionsShouldPass() throws JOSEException {
        this.setup("LIVE", "id", Collections.singletonList("GET/pat?/*"));
        client.setId(clientId);
        client.setClientId(clientId);
        when(mongoClientRepository.findByClientId(clientId)).thenReturn(Mono.just(client));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }
}
