package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.repository.MongoClientRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
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

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@DataRedisTest
@ActiveProfiles("dev")
@EnableAutoConfiguration
@ContextConfiguration(classes = {MongoClientRepository.class, Client.class, AccessControlFilter.class})
public class AccessControlFilterTests {

    @MockBean
    private MongoClientRepository mongo;

    @Autowired
    private AccessControlFilter filter;

    @MockBean
    private GatewayFilterChain filterChain  ;

    private String accessToken = "";
    private String client_id = "client-test-id";

    @BeforeEach
    public void setup() throws JOSEException, ParseException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .notBeforeTime(new Date())
                .audience("isw-core")
                .claim("client_id", client_id)
                .claim("api_resources", Arrays.asList("GET/path"))
                .jwtID(UUID.randomUUID().toString())
                .build();
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader,payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = "Bearer " + jws.serialize();
    }


    @Test
    public void testAccessControl (){
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/path")
                .header("Authorization",accessToken)
                .build();
        assertAuthorizationHeader(request);
    }

    private void assertAuthorizationHeader(MockServerHttpRequest request) {
        List<Product> testProducts = new ArrayList<>();
        Client client = new Client();
        client.setId("testclient");
        client.setClientId(client_id);
        client.setProducts(testProducts);
        when(mongo.findByClientId(client_id)).thenReturn(Mono.just(client));
        Route value = Route.async().id("testid").uri(request.getURI()).order(0)
                .predicate(swe -> true).build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, value);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        filter.filter(exchange, filterChain).block();
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete();
    }
}
