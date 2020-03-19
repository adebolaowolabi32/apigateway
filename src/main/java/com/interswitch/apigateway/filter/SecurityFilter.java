package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.handler.RouteHandlerMapping;
import com.interswitch.apigateway.route.MongoRouteDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.interswitch.apigateway.model.Endpoints.noAuthEndpoints;

@Component
public class SecurityFilter {

    private static final String NO_AUTHENTICATION_FILTER_NAME = "NoAuthentication";

    @Autowired
    private RouteHandlerMapping routeHandlerMapping;

    @Autowired
    private MongoRouteDefinitionRepository routeDefinitionRepository;

    public ReactiveAuthorizationManager<AuthorizationContext> authorize() {
        AtomicBoolean authorized = new AtomicBoolean(false);
        return (authenticationMono, context) -> {
            ServerWebExchange exchange = context.getExchange();
            String requestPath = exchange.getRequest().getPath().toString();
            if (noAuthEndpoints.stream().anyMatch(endpoint -> requestPath.matches(endpoint)))
                return Mono.just(new AuthorizationDecision(true));
            else {
                return routeHandlerMapping.lookupRoute(exchange).flatMap(route ->
                        routeDefinitionRepository.getRouteDefinitions()
                                .filter(routeDefinition ->
                                        routeDefinition.getId().equals(route.getId())).take(1).single()
                                .flatMap(routeDefinition -> {
                                    routeDefinition.getFilters().stream()
                                            .filter(filterDefinition -> filterDefinition.getName().equals(NO_AUTHENTICATION_FILTER_NAME))
                                            .forEach(filterDefinition -> {
                                                Map<String, String> args = filterDefinition.getArgs();
                                                String method = args.get("_genkey_0");
                                                String path = args.get("_genkey_1");
                                                if (matchMethodandPath(path, method, context.getExchange()))
                                                    authorized.set(true);
                                            });
                                    if (authorized.get()) return Mono.just(new AuthorizationDecision(true));
                                    return authenticationMono.flatMap(authentication -> Mono.just(new AuthorizationDecision(authentication.isAuthenticated())));
                                })
                ).switchIfEmpty(authenticationMono.flatMap(authentication -> Mono.just(new AuthorizationDecision(authentication.isAuthenticated()))));
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
