package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.util.FilterUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
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
@ContextConfiguration(classes = {AudienceFilter.class, FilterUtil.class})
public class AudienceFilterTests {
    @Autowired
    AudienceFilter filter;

    @Autowired
    FilterUtil filterUtil;

    @MockBean
    private WebFilterChain filterChain  ;

    private ServerWebExchange exchange;

    public void setup(String aud, String path) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .notBeforeTime(new Date())
                .audience(aud)
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader,payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        String accessToken = "Bearer " + jws.serialize();

        exchange = MockServerWebExchange.from(MockServerHttpRequest
                .get("http://localhost:8080" + path)
                .header("Authorization", accessToken)
                .build());

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
    }

    @Test
    public void requestsWithApiGatewayInAudienceClaimShouldPass() throws JOSEException {
        this.setup("api-gateway", "/anypath");
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void requestsWithoutApiGatewayInAudienceClaimShouldFail() throws JOSEException {
        this.setup("isw-core", "/anypath");
        StepVerifier.create(filter.filter(exchange, filterChain)).expectError().verify();
    }

    @Test
    public void allRequestsToExcludedEndpointsShouldPass()throws JOSEException {
       this.setup("", "/passport/api/v1/clients");
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allOptionsRequestsShouldPass() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest.options("http://localhost:8080/path").build());
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }
}
