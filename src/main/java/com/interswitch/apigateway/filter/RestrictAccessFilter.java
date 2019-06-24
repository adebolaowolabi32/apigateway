package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

public class RestrictAccessFilter implements WebFilter, Ordered {

    private MongoUserRepository mongoUserRepository;

    public RestrictAccessFilter(MongoUserRepository mongoUserRepository){
        this.mongoUserRepository = mongoUserRepository;
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        if(route != null){
            String username = GetUsernameFromBearerToken(exchange.getRequest().getHeaders());
            if (username != null)
                mongoUserRepository.findByUsername(username).flatMap(user -> {
                    if(user.getRole().equals(User.Role.ADMIN))
                        return chain.filter(exchange);
                    return chain.filter(exchange);

                }).subscribe();
        }
        return chain.filter(exchange);
    }

    public String GetUsernameFromBearerToken(HttpHeaders headers) {
        String username = "";
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokens = headers.get(HttpHeaders.AUTHORIZATION);
            if (accesstokens != null && !accesstokens.isEmpty()) {
                String accesstoken = accesstokens.get(0);
                if (accesstoken.contains("Bearer ")) {
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if (!accesstoken.isEmpty()) {
                        try {
                            JWT jwtToken = JWTParser.parse(accesstoken);
                            username = jwtToken.getJWTClaimsSet().getClaim("user_name").toString();
                        } catch (ParseException e) {
                            Mono.error(e).log();
                        }
                    }
                }
            }
        }
        return username;
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
