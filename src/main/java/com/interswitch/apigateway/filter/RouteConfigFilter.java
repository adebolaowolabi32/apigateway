package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.MongoRouteConfigRepository;
import com.nimbusds.jwt.JWT;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

public class RouteConfigFilter implements GlobalFilter, Ordered {

    private MongoRouteConfigRepository repository;
    private RouteDefinitionRepository routeDefinitionRepository;

    public RouteConfigFilter(MongoRouteConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String routeId = (route != null) ? route.getId() : "";
        JWT token = decodeBearerToken(headers);
        String environment = (token != null) ? getClaimAsStringFromBearerToken(token, "env") : "";
        return repository.findByRouteId(routeId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found")))
                .flatMap(config -> {
                    URI uri = null;
                    if (environment.equalsIgnoreCase("test")) {
                        uri = config.getSandboxUri();
                    }
                    if (environment.equalsIgnoreCase("uat")) {
                        uri = config.getSandboxUri();
                    }
                    if (environment.equalsIgnoreCase("prod")) {
                        uri = config.getSandboxUri();
                    }
                    exchange.getAttributes().remove(GATEWAY_ROUTE_ATTR);
                    Route.Builder builder = Route.builder();
                    RouteDefinition definition = ;
                    Route updatedRoute = builder.build();
                    exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, updatedRoute);
                    return chain.filter(exchange);
                });
//                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resource"));
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
