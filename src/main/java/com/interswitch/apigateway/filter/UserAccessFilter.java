package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.util.FilterUtil;
import com.interswitch.apigateway.util.RouteUtil;
import com.nimbusds.jwt.JWT;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class UserAccessFilter implements WebFilter, Ordered {

    private MongoUserRepository mongoUserRepository;


    private FilterUtil filterUtil;

    private RouteUtil routeUtil;

    public UserAccessFilter(MongoUserRepository mongoUserRepository, FilterUtil filterUtil, RouteUtil routeUtil){

        this.mongoUserRepository = mongoUserRepository;
        this.filterUtil = filterUtil;
        this.routeUtil = routeUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return routeUtil.isRouteBasedEndpoint(exchange).flatMap(isRouteBasedEndpoint -> {
                if(!isRouteBasedEndpoint){
                    JWT token = filterUtil.decodeBearerToken(exchange.getRequest().getHeaders());
                    String username = (token != null) ? filterUtil.getUsernameFromBearerToken(token) : "";
                    return mongoUserRepository.findByUsername(username)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrator rights to access this resource")))
                            .flatMap(user -> {
                                if(user.getRole().equals(User.Role.ADMIN))
                                    return chain.filter(exchange);
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrator rights to access this resource"));
                            });
                }
                return chain.filter(exchange);
            });
    }

    @Override
    public int getOrder() {
        return -33456778;
    }
}