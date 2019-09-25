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

import static org.mockito.Mockito.when;

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

    public void setup(String sender, String email, String aud, String path) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .claim("sender", sender)
                .claim("email", email)
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
        this.setup(null, null, null, "/actuator/prometheus");
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allRequestsToExcludedEndpointsShouldPass() throws JOSEException {
        this.setup(null, null, null, "/passport/oauth/authorize");
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsWithoutApiGatewayInAudienceClaimShouldFail() throws JOSEException {
        this.setup(null, null, null, "/path");
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"You do not have sufficient rights to this resource\"").verify();
    }

    @Test
    public void requestsToRouteBasedEndpointsShouldPass() throws JOSEException {
        this.setup(null, null, "api-gateway", "/path");
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(true));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allOtherRequestsWithoutApiGatewayClientInSenderClaimShouldFail() throws JOSEException {
        this.setup(null, null, "api-gateway", "/path");
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"You do not have permission to access this resource\"").verify();
    }

    @Test
    public void requestsToNonAdminEndpointsFromNonAdminInterswitchUserShouldPass() throws JOSEException {
        this.setup("api-gateway-client", "nonadmin@interswitch.com", "api-gateway", "/path");
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsToAdminEndpointsFromNonAdminInterswitchUserShouldFail() throws JOSEException {
        this.setup("api-gateway-client", "nonadmin@interswitch.com", "api-gateway", "golive/approve");
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        user.setUsername("");
        user.setRole(User.Role.USER);
        when(mongoUserRepository.findByUsername("")).thenReturn(Mono.just(user));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"You need administrative rights to access this resource\"").verify();
    }

    @Test
    public void allRequestsFromAnAdminInterswitchUserShouldPass() throws JOSEException {
        this.setup("api-gateway-client", "admin@interswitch.com", "api-gateway", "/golive/decline");
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        user.setUsername("");
        user.setRole(User.Role.ADMIN);
        when(mongoUserRepository.findByUsername("")).thenReturn(Mono.just(user));

        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsToDevEndpointsFromAnyUserShouldPass() throws JOSEException {
        this.setup("api-gateway-client", null, "api-gateway", "projects/testprojectId");
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsToNonDevEndpointsFromNonInterswitchUserShouldFail() throws JOSEException {
        this.setup("api-gateway-client", null, "api-gateway", "/users");
        when(routeUtil.isRouteBasedEndpoint(exchange)).thenReturn(Mono.just(false));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"Only Interswitch domain users can access this resource\"").verify();
    }

}
