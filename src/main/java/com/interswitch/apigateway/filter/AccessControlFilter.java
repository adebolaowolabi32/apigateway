package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.MongoClientRepository;
import com.nimbusds.jwt.JWT;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.interswitch.apigateway.util.FilterUtil.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;


public class AccessControlFilter implements GlobalFilter, Ordered  {

    private MongoClientRepository repository;

    private static List<String> PERMIT_ALL = Collections.singletonList("passport");
    private static List<String> ALLOW_ALL = Arrays.asList("uat", "test", "dev", "sandbox");


    public AccessControlFilter(MongoClientRepository repository) {
        this.repository = repository;
    }

    private static String wildcardToRegex(String wildcard) {
        String regex = wildcard.trim();
        String firstRegex = (regex.contains("*")) ? regex.replace("*", ".*") : regex;
        regex = (firstRegex.contains("?")) ? firstRegex.replace("?", ".") : firstRegex;
        if (StringUtils.containsAny(regex, "()&][$^{}|")) {
            regex = regex.replaceAll("[()&\\]\\[$^{}|]", "");
        }
        return regex;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        HttpHeaders headers = exchange.getRequest().getHeaders();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String routeId = (route != null) ? route.getId() : "";
        JWT token = decodeBearerToken(headers);
        String environment = (token != null) ? getClaimAsStringFromBearerToken(token, "env") : "";

        if (PERMIT_ALL.contains(routeId) || ALLOW_ALL.contains(environment.toLowerCase()) || HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()))
            return chain.filter(exchange);
        String clientId = (token != null) ? getClaimAsStringFromBearerToken(token, "client_id") : "";
        List<String> resources = (token != null) ? getClaimAsListFromBearerToken(token, "api_resources") : Collections.emptyList();

        return repository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Client not found")))
                .flatMap(clients -> {
                    for(var r : resources) {
                        r = r.replaceAll(" ", "");
                        int indexOfFirstSlash = r.indexOf('/');
                        String method = r.substring(0, indexOfFirstSlash);
                        String path = r.substring(indexOfFirstSlash);
                        if (exchange.getRequest().getPath().toString().matches(wildcardToRegex(path)))
                            if (exchange.getRequest().getMethodValue().equals(method))
                                return chain.filter(exchange);
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resource"));
                });
    }

    @Override
    public int getOrder() {
        return 1;
    }
}