package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.util.SecurityUtil;
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

import java.util.List;

import static com.interswitch.apigateway.model.Endpoints.PASSPORT_ROUTE_ID;
import static com.interswitch.apigateway.model.Endpoints.noAuthEndpoints;
import static com.interswitch.apigateway.util.FilterUtil.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

public class RouteAccessControlFilter implements GlobalFilter, Ordered {

    private SecurityUtil securityUtil;

    public RouteAccessControlFilter(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    private static String wildcardToRegex(String wildcard) {
        String regex = wildcard.trim();
        if (regex.contains("*")) regex = regex.replace("*", ".*");
        if (regex.contains("?")) regex = regex.replace("?", ".");
        if (StringUtils.containsAny(regex, "()&][$^{}|")) {
            regex = regex.replaceAll("[()&\\]\\[$^{}|]", "");
        }
        return regex;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String requestPath = exchange.getRequest().getPath().toString();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String routeId = (route != null) ? route.getId() : "";
        JWT token = decodeBearerToken(headers);
        String environment = getClaimAsStringFromBearerToken(token, "env");

        if (PASSPORT_ROUTE_ID.equals(routeId) || match(requestPath, noAuthEndpoints) || environment.equalsIgnoreCase(Env.TEST.toString()) || HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()))
            return chain.filter(exchange);
        return securityUtil.isRequestAuthenticated(route, exchange).flatMap(isRequestAuthenticated -> {
            if (isRequestAuthenticated.get())
                return chain.filter(exchange);
            List<String> resources = getClaimAsListFromBearerToken(token, "api_resources");
            for (var r : resources) {
                r = r.replaceAll(" ", "");
                int indexOfFirstSlash = r.indexOf('/');
                String method = r.substring(r.indexOf("-") + 1, indexOfFirstSlash);
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