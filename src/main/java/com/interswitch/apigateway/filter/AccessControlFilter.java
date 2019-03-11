package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.List;


public class AccessControlFilter implements GlobalFilter, Ordered  {

    private ClientCacheRepository repository;

    public  AccessControlFilter(ClientCacheRepository repository) {
        this.repository=repository;

    }
    String client_id = "";
    JWT jwtToken = null;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String accessToken = exchange.getRequest().getHeaders().get("Authorization").toString();
        accessToken = accessToken.replace(accessToken.substring(accessToken.indexOf("B") - 1, accessToken.indexOf(" ") + 1), "");
        String resourceId = (exchange.getRequest().getMethod().toString()) + (exchange.getRequest().getPath().toString());

        try {
            jwtToken = JWTParser.parse(accessToken);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            client_id =jwtToken.getJWTClaimsSet().getClaim("client_id").toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
            return check(resourceId)
                    .flatMap(condition -> {
                                if (condition.equals(true)) {
                                    return chain.filter(exchange);
                                } else {
                                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resource"));

                                }
                            }
                    );

    }
    private Mono<Boolean> check(String resourceId) {
            return repository.findByClientId(client_id)
                .switchIfEmpty(Mono.error(new Exception()))
                .flatMap(clientResources -> {
                    List resourceIds = clientResources.getResourceIds();
                    if (resourceIds.contains(resourceId)) {
                        return Mono.just(true);
                    } else {
                        return Mono.just(false);
                    }
                });

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
