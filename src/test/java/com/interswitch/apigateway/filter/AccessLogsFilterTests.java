package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.model.AccessLogs.Entity;
import com.interswitch.apigateway.model.AccessLogs.MethodActions;
import com.interswitch.apigateway.model.AccessLogs.Status;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class AccessLogsFilterTests {
    @Autowired
    AccessLogsFilter filter;

    private static String accessToken;
    private static String email;
    private static String client;
    @MockBean
    private MongoAccessLogsRepository mongoAccessLogsRepository;
    @MockBean
    private WebFilterChain filterChain;
    private ArgumentCaptor<AccessLogs> captor = ArgumentCaptor.forClass(AccessLogs.class);
    private ServerWebExchange exchange;
    private MockServerHttpRequest request;
    private AccessLogs accessLogs;

    @BeforeAll
    public static void setup() throws JOSEException {
        email = "sample@email.com";
        client = "clientId";
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime() + 1000 * 60 ^ 10))
                .notBeforeTime(new Date())
                .claim("email", email)
                .claim("client_id", client)
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader, payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = "Bearer " + jws.serialize();
    }

    @Test
    public void allGetRequestsShouldSkipAudit() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/anyPath")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allOptionsRequestsShouldSkipAudit() {
        MockServerHttpRequest request = MockServerHttpRequest
                .options("/anyPath")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void externalRouteBasedRequestsShouldSkipAudit() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/passport/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

    @Test
    public void allInternalPostRequestsShouldBeAudited() {
        request = MockServerHttpRequest
                .post("/projects")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .build();

        filterExchangeThenAssertAuditLogs("/projects", MethodActions.CREATE, Entity.PROJECT, "");
    }

    @Test
    public void allInternalPutRequestsShouldBeAudited() {
        request = MockServerHttpRequest
                .put("/products")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .build();

        filterExchangeThenAssertAuditLogs("/products", MethodActions.UPDATE, Entity.PRODUCT, "");
    }

    @Test
    public void allInternalDeleteRequestsShouldBeAudited() {
        String id = "username";
        request = MockServerHttpRequest
                .delete("/users/" + id)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .build();

        filterExchangeThenAssertAuditLogs("/users/" + id, MethodActions.DELETE, Entity.USER, id);
    }

    @Test
    public void testRefreshEndpointAudit() {
        request = MockServerHttpRequest
                .post("/actuator/gateway/refresh")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .build();

        filterExchangeThenAssertAuditLogs("/actuator/gateway/refresh", MethodActions.REFRESH, Entity.REFRESH, "");
    }

    @Test
    public void testRouteCreateEndpointAudit() {
        String id = "routeId";
        request = MockServerHttpRequest
                .post("/actuator/gateway/routes/" + id)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .build();

        filterExchangeThenAssertAuditLogs("/actuator/gateway/routes/" + id, MethodActions.CREATE, Entity.ROUTE, id);
    }

    private void filterExchangeThenAssertAuditLogs(String path, Object action, Entity entity, String id) {
        exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.OK);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        when(mongoAccessLogsRepository.save(captor.capture())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();

        accessLogs = captor.getValue();
        assertThat(accessLogs.getApi()).isEqualTo(path);
        assertThat(accessLogs.getUsername()).isEqualTo(email);
        assertThat(accessLogs.getClient()).isEqualTo(client);
        assertThat(accessLogs.getAction()).isEqualTo(action);
        assertThat(accessLogs.getEntity()).isEqualTo(entity);
        assertThat(accessLogs.getEntityId()).isEqualTo(id);
        assertThat(accessLogs.getStatus()).isBetween(Status.SUCCESSFUL, Status.FAILED);
        assertThat(accessLogs.getTimestamp()).isInstanceOf(LocalDateTime.class);
    }
}
