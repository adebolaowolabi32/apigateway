package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.config.CacheConfig;
import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.repository.ClientCacheRepository;

import com.interswitch.apigateway.repository.ClientMongoRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataRedisTest
@ActiveProfiles("dev")
@EnableAutoConfiguration
@ContextConfiguration(classes = {CacheConfig.class,ClientCacheRepository.class, ClientMongoRepository.class, AccessControlFilter.class})
public class AccessControlFliterTests {

    @Autowired
    private ClientCacheRepository repository;
    @MockBean
    private ClientMongoRepository mongo;

    private GlobalFilter filter;
    private GatewayFilterChain filterChain  ;

    private List testresourceIds = new ArrayList();
    private List origins = new ArrayList();
    private Client client= new Client();
    private String  clientId = "testclientid";
    private String accessToken = "";

    @BeforeEach
    public void setup() throws JOSEException, ParseException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .expirationTime(new Date(new Date().getTime()+1000*60^10))
                .notBeforeTime(new Date())
                .audience("isw-core")
                .claim("client_id", "IKIA344B890097001647EEDB60226A5850AE75C7CD19")
                .jwtID(UUID.randomUUID().toString())
                .build();
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jws = new JWSObject(jwsHeader,payload);
        jws.sign(new MACSigner("AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow"));
        accessToken = "Bearer " + jws.serialize();

        filter = new AccessControlFilter(repository);
        filterChain = mock(GatewayFilterChain.class);
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        origins.add("http://localhost:8080");
        client = new Client("id",clientId,origins,testresourceIds);
    }

    @Test
    public void testAccessControl (){
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/foo")
                .header("Authorization",accessToken)
                .build();
        assertAuthorizationHeader(request);
    }


    public void assertAuthorizationHeader(MockServerHttpRequest request) {
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());
        filter.filter(exchange, filterChain).block();
        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete();

    }
}
