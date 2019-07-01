package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.util.FilterUtil;
import com.nimbusds.jwt.JWT;
import org.springframework.core.Ordered;
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

public class AudienceFilter implements WebFilter, Ordered {
    private static List<String> passportRoutes = Arrays.asList("/passport/oauth/token", "/passport/api/v1/accounts", "/passport/api/v1/clients");
    private FilterUtil filterUtil;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Iterator<String> passportIterate = passportRoutes.iterator();
        JWT token = filterUtil.decodeBearerToken(exchange.getRequest().getHeaders());
        String exchangePath = (token != null) ? exchange.getRequest().getPath().toString() : "";
        List<String> audience = (token != null) ? filterUtil.getAudienceFromBearerToken(token) : Collections.emptyList();
                while (passportIterate.hasNext())
                if (audience.contains("api-gateway") || exchangePath.contains(passportIterate.next()))
                    return chain.filter(exchange);
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have sufficient rights to this resource"));
    }
    public AudienceFilter(FilterUtil filterUtil){
        this.filterUtil=filterUtil;
    }

    @Override
    public int getOrder() {
        return -33456778;
    }
}
