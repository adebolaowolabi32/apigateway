package com.interswitch.apigateway.util;

import com.interswitch.apigateway.route.MongoRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecurityUtil {

    private static final String NO_AUTHENTICATION_FILTER_NAME = "NoAuthentication";

    private MongoRouteDefinitionRepository routeDefinitionRepository;

    public SecurityUtil(MongoRouteDefinitionRepository routeDefinitionRepository) {
        this.routeDefinitionRepository = routeDefinitionRepository;
    }

    public Mono<AtomicBoolean> isRequestAuthenticated(Route route, ServerWebExchange exchange) {
        AtomicBoolean authorized = new AtomicBoolean(false);
        return routeDefinitionRepository.getRouteDefinitions()
                .filter(routeDefinition ->
                        routeDefinition.getId().equals(route.getId())).take(1).single()
                .flatMap(routeDefinition -> {
                    routeDefinition.getFilters().stream()
                            .filter(filterDefinition -> filterDefinition.getName().equals(NO_AUTHENTICATION_FILTER_NAME))
                            .forEach(filterDefinition -> {
                                Map<String, String> args = filterDefinition.getArgs();
                                String method = args.get("_genkey_0");
                                String path = args.get("_genkey_1");
                                if (matchMethodandPath(path, method, exchange))
                                    authorized.set(true);
                            });
                    return Mono.just(authorized);
                });
    }

    private boolean matchMethodandPath(String path, String method, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().toString();
        String requestMethod = request.getMethodValue();
        return requestPath.matches(path) && requestMethod.equalsIgnoreCase(method);
    }
}
