package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.config.CacheConfig;
import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@DataRedisTest
@ActiveProfiles("dev")
@EnableAutoConfiguration
@ContextConfiguration(classes = {CacheConfig.class,ClientCacheRepository.class, ClientMongoRepository.class, AccessControlFilter.class})
public class AccessControlFliterTests {

    @MockBean
    private ClientCacheRepository repository;
    @MockBean
    private ClientMongoRepository mongo;

    private GlobalFilter filter;
    private GatewayFilterChain filterChain  ;
    private String accessToken = "";
    private String client_id = "IKIA344B890097001647EEDB60226A5850AE75C7CD19";
    List testresourceIds = new ArrayList();
    List origin=null;

    @BeforeEach
    public void setup() throws JOSEException, ParseException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .notBeforeTime(new Date())
                .audience("isw-core")
                .claim("client_id", client_id)
                .jwtID(UUID.randomUUID().toString())
                .build();
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader,payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = "Bearer " + jws.serialize();
        filter = new AccessControlFilter(repository);
        filterChain = mock(GatewayFilterChain.class);
        testresourceIds.add("testid");
    }


    @Test
    public void testAccessControl (){
        String routeUri = "http://localhost:8080/users/find/TestP.jaMonJan14";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/users/find/TestP.jaMonJan14")
                .header("Authorization",accessToken)
                .build();
        assertAuthorizationHeader(request);
    }

    @Test
    private void assertAuthorizationHeader(MockServerHttpRequest request) {
        Client client = new Client("testclient",client_id,origin,testresourceIds);
        when(repository.findByClientId(client_id)).thenReturn(Mono.just(client));
        Route value = Route.async().id("testid").uri(request.getURI()).order(0)
                .predicate(swe -> true).build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, value);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        filter.filter(exchange, filterChain).block();
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete();

    }
}
