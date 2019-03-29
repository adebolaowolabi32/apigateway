package com.interswitch.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.RequestPath;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AccessCheck implements ReactiveAuthorizationManager<AuthorizationContext> {
    @Value("${patterns.match}")
    private String match;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
        ServerWebExchange exchange = object.getExchange();
        String requestPath = String.valueOf(exchange.getRequest().getPath());
        List<String> matchers = new ArrayList<String>(Arrays.asList(match.split(",")));
         if (matchers.contains(requestPath)) {
                return Mono.just(new AuthorizationDecision(true));
            } else {
                return authentication.flatMap(authentication1 ->
                        Mono.just(new AuthorizationDecision(authentication1.isAuthenticated()))
                );
            }
        }

}


