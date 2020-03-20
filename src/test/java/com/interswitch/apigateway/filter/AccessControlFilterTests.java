package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.util.RouteUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpStatus;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {AccessControlFilter.class, RouteUtil.class})
public class AccessControlFilterTests {
    @Autowired
    private AccessControlFilter filter;

    @MockBean
    private MongoUserRepository mongoUserRepository;

    @MockBean
    private RouteUtil routeUtil;

    @MockBean
    private WebFilterChain filterChain;

    private ServerWebExchange exchange;

    private User user = new User();

    private String username = "username";

    public void setup(String sender, String email, String username, String aud, String path) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .claim("sender", sender)
                .claim("email", email)
                .claim("user_name", username)
                .audience(aud)
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader,payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        String accessToken = "Bearer " + jws.serialize();

        exchange = MockServerWebExchange.from(MockServerHttpRequest
                .post("http://localhost:8080" + path)
                .header("Authorization", accessToken)
                .build());

        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, Route.async().id("").uri("http://localhost:8080").order(0)
                .predicate(swe -> true).build());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
    }

    @Test
    public void allOptionsRequestsShouldPass() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest
                .options("http://localhost:8080/path")
                .build());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allRequestsToSystemEndpointShouldPass() throws JOSEException {
        this.setup(null, null, null, null, "/actuator/prometheus");
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allRequestsToExcludedEndpointsShouldPass() throws JOSEException {
        this.setup(null, null, null, null, "/passport/oauth/authorize");
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsWithoutApiGatewayInAudienceClaimShouldFail() throws JOSEException {
        this.setup(null, null, null, null, "/path");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"You do not have sufficient rights to this resource\"").verify();
    }

    @Test
    public void requestsToRouteBasedEndpointsShouldPass() throws JOSEException {
        this.setup(null, null, null, "api-gateway", "/path");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(true));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allOtherRequestsWithoutApiGatewayClientInSenderClaimShouldFail() throws JOSEException {
        this.setup(null, null, null, "api-gateway", "/path");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"You do not have permission to access this resource\"").verify();
    }

    @Test
    public void allRequestsWithoutUserTokenToNonRouteBasedEndpointsShouldFail() throws JOSEException {
        this.setup("api-gateway-client", null, null, "api-gateway", "/path");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"A user token is required to access this resource\"").verify();
    }

    @Test
    public void requestsToNonAdminEndpointsFromNonAdminInterswitchUserShouldPass() throws JOSEException {
        this.setup("api-gateway-client", "nonadmin@interswitch.com", username, "api-gateway", "/path");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsToAdminEndpointsFromNonAdminInterswitchUserShouldFail() throws JOSEException {
        this.setup("api-gateway-client", "nonadmin@interswitch.com", username, "api-gateway", "golive/approve");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        user.setRole(User.Role.USER);
        when(mongoUserRepository.findByUsername("nonadmin@interswitch.com")).thenReturn(Mono.just(user));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"You need administrative rights to access this resource\"").verify();
    }

    @Test
    public void allRequestsFromAnAdminInterswitchUserShouldPass() throws JOSEException {
        this.setup("api-gateway-client", "admin@interswitch.com", username, "api-gateway", "/golive/decline");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        user.setRole(User.Role.ADMIN);
        when(mongoUserRepository.findByUsername("admin@interswitch.com")).thenReturn(Mono.just(user));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsToDevEndpointsFromAnyUserShouldPass() throws JOSEException {
        this.setup("api-gateway-client", null, username, "api-gateway", "projects/testprojectId");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsToNonDevEndpointsFromNonInterswitchUserShouldFail() throws JOSEException {
        this.setup("api-gateway-client", null, username, "api-gateway", "/users");
        when(routeUtil.isRequestAuthenticated(any(ServerWebExchange.class))).thenReturn(Mono.just(new AtomicBoolean(false)));
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"Only Interswitch domain users can access this resource\"").verify();
    }

}
