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

import java.util.List;

public class AudienceFilter implements WebFilter, Ordered {
    private static String PassportToken= "/passport/oauth/token";

    private FilterUtil filterUtil;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        JWT token = filterUtil.DecodeBearerToken(exchange.getRequest().getHeaders());
        if(token!=null) {
            String exchangePath = exchange.getRequest().getPath().toString();
            List<String> audience = filterUtil.GetAudience(token);
            String environment = filterUtil.GetEnvironment(token);
            if (audience.contains("api-gateway")||environment == "TEST" ||exchangePath == PassportToken)
                return chain.filter(exchange);
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have sufficient rights to this resource"));
        }
        else{
            return chain.filter(exchange);
        }
    }
    public AudienceFilter(FilterUtil filterUtil){
        this.filterUtil=filterUtil;
    }

    @Override
    public int getOrder() {
        return -33456778;
    }
}
