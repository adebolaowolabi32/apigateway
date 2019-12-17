package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.util.RouteUtil;
import com.nimbusds.jwt.JWT;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.interswitch.apigateway.model.Endpoints.*;
import static com.interswitch.apigateway.util.FilterUtil.*;

public class AccessControlFilter implements WebFilter, Ordered {

    private MongoUserRepository mongoUserRepository;

    private RouteUtil routeUtil;

    public AccessControlFilter(MongoUserRepository mongoUserRepository, RouteUtil routeUtil) {
        this.mongoUserRepository = mongoUserRepository;
        this.routeUtil = routeUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()) || match(path, noAuthEndpoints))
            return chain.filter(exchange);
        JWT token = decodeBearerToken(exchange.getRequest().getHeaders());
        List<String> audience = getClaimAsListFromBearerToken(token, "aud");
        if (audience.contains("api-gateway")) {
            return routeUtil.isRouteBasedEndpoint(exchange).flatMap(isRouteBasedEndpoint -> {
                if (!isRouteBasedEndpoint) {
                    String sender = getClaimAsStringFromBearerToken(token, "sender");
                    if (sender.equals("api-gateway-client")) {
                        String username = getClaimAsStringFromBearerToken(token, "user_name");
                        if (username.isEmpty())
                            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "A user token is required to access this resource"));
                        String email = getClaimAsStringFromBearerToken(token, "email");
                        if (isInterswitchEmail(email)) {
                            if (match(path, adminEndpoints)) {
                                return mongoUserRepository.findByUsername(email)
                                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrative rights to access this resource")))
                                        .flatMap(user -> {
                                            if (user.getRole().equals(User.Role.ADMIN))
                                                return chain.filter(exchange);
                                            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrative rights to access this resource"));
                                        });
                            }
                            return chain.filter(exchange);
                        }
                        if (match(path, devEndpoints))
                            return chain.filter(exchange);
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Interswitch domain users can access this resource"));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource"));
                }
                return chain.filter(exchange);
            });
        }
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have sufficient rights to this resource"));
    }

    @Override
    public int getOrder() {
        return -80;
    }
}