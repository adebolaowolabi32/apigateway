package com.interswitch.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

@Configuration
public class SecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.keyValue}")
    private String key;


    @Bean
    public ReactiveJwtDecoder jwtDecoder() throws InvalidKeySpecException, NoSuchAlgorithmException {
        RSAPublicKey publicKey = (RSAPublicKey) publicKey();
        NimbusReactiveJwtDecoder jwtDecoder = new NimbusReactiveJwtDecoder(publicKey);
        OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
                Arrays.asList(new JwtTimestampValidator(Duration.ofSeconds(60)),
                        JwtValidators.createDefault()));
        jwtDecoder.setJwtValidator(withClockSkew);
        return jwtDecoder;
    }

    private PublicKey publicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keybytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keybytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
