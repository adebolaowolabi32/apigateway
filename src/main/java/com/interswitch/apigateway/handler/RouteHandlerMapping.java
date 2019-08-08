package com.interswitch.apigateway.handler;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.repository.MongoEnvRepository;
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

import java.net.URI;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;

@Component
public class RouteHandlerMapping extends RoutePredicateHandlerMapping {
    private MongoEnvRepository repository;

    public RouteHandlerMapping(FilteringWebHandler webHandler, RouteLocator routeLocator, GlobalCorsProperties globalCorsProperties, Environment environment, MongoEnvRepository repository) {
        super(webHandler, routeLocator, globalCorsProperties, environment);
        this.repository = repository;
    }

    @Override
    public Mono<Route> lookupRoute(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        JWT token = decodeBearerToken(headers);
        String environment = (token != null) ? getClaimAsStringFromBearerToken(token, "env") : "";
        Mono<Route> lookupRoute = super.lookupRoute(exchange);
        return lookupRoute.flatMap(route -> {
            return repository.findByRouteId(route.getId())
                    .flatMap(config -> {
                        URI uri = route.getUri();
                        if (environment.equalsIgnoreCase(Env.environment.TEST.toString()) || environment.equalsIgnoreCase(Env.environment.SANDBOX.toString())) {
                            uri = (config.getSandbox() != null) ? URI.create(config.getSandbox()) : route.getUri();
                        }
                        if (environment.equalsIgnoreCase(Env.environment.UAT.toString()) || environment.equalsIgnoreCase(Env.environment.DEV.toString())) {
                            uri = (config.getUat() != null) ? URI.create(config.getUat()) : route.getUri();
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("Route matched: " + route.getId());
                        }
                        return Mono.just(Route.async().id(route.getId()).uri(uri).order(0).asyncPredicate(route.getPredicate()).filters(route.getFilters())
                                .build());
                    }).switchIfEmpty(Mono.error(new NotFoundException("Route Environment configuration not found")));
        });

    }
}