package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.util.FilterUtil;
import com.interswitch.apigateway.util.RouteUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.when;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {UserAccessFilter.class, FilterUtil.class, RouteUtil.class})
public class UserAccessFilterTests {
    @Autowired
    private UserAccessFilter filter;

    @MockBean
    private MongoUserRepository mongoUserRepository;

    @MockBean
    private RouteUtil routeUtil;

    @MockBean
    private WebFilterChain filterChain  ;

    private ServerWebExchange exchange;

    private User user = new User();

    private String username = "username";

    @BeforeEach
    public void setup() throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .notBeforeTime(new Date())
                .claim("user_name", username)
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader,payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        String accessToken = "Bearer " + jws.serialize();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/path")
                .header("Authorization", accessToken)
                .build();

        exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        user.setUsername(username);
    }

    @Test
    public void allRequestsToRouteBasedEndpointsShouldPass(){
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(true));

        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void adminRequestsToNonRouteBasedEndpointsShouldPass(){
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));

        user.setRole(User.Role.ADMIN);
        when(mongoUserRepository.findByUsername(username)).thenReturn(Mono.just(user));

        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void userRequestsToNonRouteBasedEndpointsShouldFail(){
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));

        user.setRole(User.Role.USER);
        when(mongoUserRepository.findByUsername(username)).thenReturn(Mono.just(user));

        StepVerifier.create(filter.filter(exchange, filterChain)).expectError().verify();
    }

    @Test
    public void allRequestsToExcludedEndpointsShouldPass(){
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/actuator/health")
                .build();

        exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));

        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }
}
