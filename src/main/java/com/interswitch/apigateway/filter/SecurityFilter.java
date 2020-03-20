package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.util.RouteUtil;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.interswitch.apigateway.model.Endpoints.noAuthEndpoints;

public class SecurityFilter {

    private RouteUtil routeUtil;

    public SecurityFilter(RouteUtil routeUtil) {
        this.routeUtil = routeUtil;
    }

    public ReactiveAuthorizationManager<AuthorizationContext> authorize() {
        return (authenticationMono, context) -> {
            ServerWebExchange exchange = context.getExchange();
            String requestPath = exchange.getRequest().getPath().toString();
            if (noAuthEndpoints.stream().anyMatch(endpoint -> requestPath.matches(endpoint)))
                return Mono.just(new AuthorizationDecision(true));
            else {
                return routeUtil.isRequestAuthenticated(exchange).flatMap(authorized -> {
                    if (authorized.get()) return Mono.just(new AuthorizationDecision(true));
                    return authenticationMono.flatMap(authentication -> Mono.just(new AuthorizationDecision(authentication.isAuthenticated())));
                });
            }
        };
    }

    private boolean matchMethodandPath(String path, String method, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().toString();
        String requestMethod = request.getMethodValue();
        return requestPath.matches(path) && requestMethod.equalsIgnoreCase(method);
    }
}
