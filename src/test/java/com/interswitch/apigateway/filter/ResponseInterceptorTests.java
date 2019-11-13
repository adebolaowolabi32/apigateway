package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.Env;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.mockito.Mockito.when;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {ResponseInterceptor.class})
public class ResponseInterceptorTests {
    @Autowired
    private ResponseInterceptor filter;

    @MockBean
    private WebFilterChain filterChain;

    private ArgumentCaptor<ServerWebExchange> exchangeArgumentCaptor;

    private ServerWebExchange exchange;

    public void setup(String env) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .claim("env", env)
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        String accessToken = "Bearer " + jws.serialize();

        exchange = MockServerWebExchange.from(MockServerHttpRequest
                .post("http://localhost:8080/path")
                .header("Authorization", accessToken)
                .build());

        exchangeArgumentCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
    }

    @Test
    public void liveRequestsShouldPassThrough() throws JOSEException {
        this.setup(Env.LIVE.toString());
        when(filterChain.filter(exchangeArgumentCaptor.capture())).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void testRequestsWithoutSpecificErrorMessageShouldPassThrough() throws JOSEException {
        this.setup(Env.TEST.toString());
        when(filterChain.filter(exchangeArgumentCaptor.capture())).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void testRequestsWithSpecificErrorMessageShouldBeIntercepted() throws JOSEException {
        this.setup(Env.TEST.toString());
        when(filterChain.filter(exchangeArgumentCaptor.capture())).thenReturn(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Your access token is no longer valid, kindly refresh your credentials on developer console.")));
        StepVerifier.create(filter.filter(exchange, filterChain)).expectErrorMessage(HttpStatus.FORBIDDEN + " \"Your access token is no longer valid, kindly refresh your credentials on developer console.\"").verify();
    }

}
