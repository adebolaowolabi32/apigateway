package com.interswitch.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class KeyResolverConfig {
    @Bean
    KeyResolver apiKeyResolver(){
        return exchange -> {
                return exchange.getPrincipal().map(Principal::getName).switchIfEmpty(Mono.empty());
            };

    }
}
