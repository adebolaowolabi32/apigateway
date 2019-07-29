package com.interswitch.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.keyValue}")
    private String key;

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        http.exceptionHandling()
                .authenticationEntryPoint((exchange, exception) -> Mono.fromRunnable(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                }))
                .accessDeniedHandler((serverWebExchange, exception) -> Mono.fromRunnable(() -> {
                    serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                })).and().authorizeExchange()
                .pathMatchers("/passport/oauth/**", "/actuator/prometheus", "/actuator/health", "**/login", "**/logout", "**/register", "**/signup").permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .anyExchange().authenticated()
                .and().csrf().disable()
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