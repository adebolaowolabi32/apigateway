package com.interswitch.apigateway.filter;

import com.nimbusds.jwt.JWT;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsListFromBearerToken;

public class AudienceFilter implements WebFilter, Ordered {
    private static List<String> excludedEndpoints = Arrays.asList("/passport/oauth/token", "/passport/oauth/authorize", "/passport/api/v1/accounts", "/passport/api/v1/clients", "/actuator/health", "/actuator/prometheus");

    public AudienceFilter() {
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        boolean isExcluded = false;
        Iterator<String> iterator = excludedEndpoints.iterator();
        JWT token = decodeBearerToken(exchange.getRequest().getHeaders());
        String exchangePath = exchange.getRequest().getPath().toString();
        List<String> audience = (token != null) ? getClaimAsListFromBearerToken(token, "aud") : Collections.emptyList();
        while (iterator.hasNext()) {
            if (exchangePath.contains(iterator.next())) isExcluded = true;
        }
        if (audience.contains("api-gateway") || isExcluded || HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()))
            return chain.filter(exchange);
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have sufficient rights to this resource"));
    }

    @Override
    public int getOrder() {
        return -99;
    }
}