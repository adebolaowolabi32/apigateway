package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@EnableWebFluxSecurity
public class SecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.keyValue}")
    private String key;

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        http.authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .anyExchange().access(securityFilter.authorize()).and().csrf().disable()
            .oauth2ResourceServer()
            .jwt().publicKey((RSAPublicKey) publicKey());
        return http.build();
    }

    private PublicKey publicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keybytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keybytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}