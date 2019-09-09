package com.interswitch.apigateway.handler;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.repository.MongoRouteEnvironmentRepository;
import com.nimbusds.jwt.JWT;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;

@Component
public class RouteHandlerMapping extends RoutePredicateHandlerMapping {
    private MongoRouteEnvironmentRepository repository;

    public RouteHandlerMapping(FilteringWebHandler webHandler, RouteLocator routeLocator, GlobalCorsProperties globalCorsProperties, Environment environment, MongoRouteEnvironmentRepository repository) {
        super(webHandler, routeLocator, globalCorsProperties, environment);
        this.repository = repository;
    }

    @Override
    public Mono<Route> lookupRoute(ServerWebExchange exchange) {
        Mono<Route> lookupRoute = super.lookupRoute(exchange);
        return lookupRoute.flatMap(route -> {
            return repository.findByRouteId(route.getId())
                    .flatMap(config -> {
                        String environment = "";
                        if (route.getId().equals("passport") && exchange.getRequest().getQueryParams().getFirst("env") != null) {
                            environment = exchange.getRequest().getQueryParams().getFirst("env");
                        } else {
                            JWT token = decodeBearerToken(exchange.getRequest().getHeaders());
                            environment = getClaimAsStringFromBearerToken(token, "env");
                        }
                        URI uri = route.getUri();
                        if (environment.equalsIgnoreCase(Env.TEST.toString())) {
                            uri = (config.getTestURL() != null) ? URI.create(config.getTestURL()) : route.getUri();
                        }
                        return Mono.just(Route.async().id(route.getId()).uri(uri).order(route.getOrder()).asyncPredicate(route.getPredicate()).filters(route.getFilters())
                                .build());
                    }).switchIfEmpty(Mono.error(new NotFoundException("Route Environment configuration not found")));
        });
    }
}