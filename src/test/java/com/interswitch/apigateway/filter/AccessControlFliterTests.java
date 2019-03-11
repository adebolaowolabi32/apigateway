package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.config.CacheConfig;
import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ClientResourcesRepository;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataRedisTest
@ActiveProfiles("dev")
@EnableAutoConfiguration
@ContextConfiguration(classes = {CacheConfig.class,MongoClientResourcesRepository.class,ClientResourcesRepository.class, AccessControlFilter.class})
public class AccessControlFliterTests {

    @Autowired
    private ClientResourcesRepository repository;
    @MockBean
    private MongoClientResourcesRepository mongo;

    private GlobalFilter filter;
    private GatewayFilterChain filterChain  ;

    private List testresourceIds = new ArrayList();
    private ClientResources resource;
    private String  clientId = "testclientid";
    private String accessToken = "";

    @BeforeEach
    public void setup() throws JOSEException, ParseException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("client_id", "IKIA344B890097001647EEDB60226A5850AE75C7CD19")
                .build();
        JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256);
        Payload payload = new Payload(claims.toJSONObject());
        JWEObject jweObject = new JWEObject(header, payload);
        String secret = "841D8A6C80CBA4FCAD32D5367C18C53B";
        byte[] secretKey = secret.getBytes();
        DirectEncrypter encrypter = new DirectEncrypter(secretKey);
        jweObject.encrypt(encrypter);
        Payload pay = jweObject.getPayload();
        accessToken = jweObject.serialize();
        JWT jwtToken = JWTParser.parse(accessToken);
        String client_id = jwtToken.getJWTClaimsSet().getClaim("client_id").toString();

        filter = new AccessControlFilter(repository);
        filterChain = mock(GatewayFilterChain.class);
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        resource = new ClientResources("id",clientId,testresourceIds);
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
