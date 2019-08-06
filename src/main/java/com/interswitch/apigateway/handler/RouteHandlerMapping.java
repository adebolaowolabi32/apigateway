package com.interswitch.apigateway.handler;

import com.interswitch.apigateway.repository.MongoRouteConfigRepository;
import com.nimbusds.jwt.JWT;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;

@Component
public class RouteHandlerMapping extends RoutePredicateHandlerMapping {
    private MongoRouteConfigRepository repository;
    private RouteLocator routeLocator;

    public RouteHandlerMapping(FilteringWebHandler webHandler, RouteLocator routeLocator, GlobalCorsProperties globalCorsProperties, Environment environment, MongoRouteConfigRepository repository) {
        super(webHandler, routeLocator, globalCorsProperties, environment);
        this.repository = repository;
        this.routeLocator = routeLocator;
    }

    @Override
    protected Mono<Route> lookupRoute(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        JWT token = decodeBearerToken(headers);
        String environment = (token != null) ? getClaimAsStringFromBearerToken(token, "env") : "";
        return this.routeLocator.getRoutes()
                .concatMap(route -> Mono.just(route).filterWhen(r -> {
                    exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, r.getId());
                    return r.getPredicate().apply(exchange);
                })
                        .doOnError(e -> logger.error(
                                "Error applying predicate for route: " + route.getId(),
                                e))
                        .onErrorResume(e -> Mono.empty()))
                .next()
                .flatMap(route -> {
                    return repository.findByRouteId(route.getId())
                            .flatMap(config -> {
                                if (environment.equalsIgnoreCase("test") || environment.equalsIgnoreCase("sandbox"))
                                    return Mono.just(Route.async().id(route.getId()).uri(config.getSandbox()).order(0).asyncPredicate(route.getPredicate())
                                            .build());
                                if (environment.equalsIgnoreCase("uat") || environment.equalsIgnoreCase("dev"))
                                    return Mono.just(Route.async().id(route.getId()).uri(config.getUat()).order(0).asyncPredicate(route.getPredicate())
                                            .build());
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Route matched: " + route.getId());
                                }
                                return Mono.just(route);
                            }).switchIfEmpty(Mono.error(new NotFoundException("Route Configuration not found")));
                });

    }
}
