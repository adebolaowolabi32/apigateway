package com.interswitch.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class FilterConfig {
    private static String API_KEY = "API-Key";

    @Bean
    KeyResolver apiKeyResolver(){
        return exchange -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            if (headers.containsKey(API_KEY)){
                return Mono.just(exchange.getRequest().getHeaders().getFirst(API_KEY));
            }
            else{
                return exchange.getPrincipal().map(Principal::getName).switchIfEmpty(Mono.empty());
            }
        };
    }
}
