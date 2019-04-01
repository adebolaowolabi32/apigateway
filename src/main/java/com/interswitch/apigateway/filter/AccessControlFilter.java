package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;


public class AccessControlFilter implements GlobalFilter, Ordered  {

    private ClientCacheRepository repository;

    private static List<String> ALLOW_ALL_ACCESS = Arrays.asList("passport-oauth");

    public  AccessControlFilter(ClientCacheRepository repository) {
        this.repository=repository;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        HttpHeaders headers = exchange.getRequest().getHeaders();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String resourceId = (route != null) ? route.getId() : "";
        String client_id = "";

        if (headers.containsKey(HttpHeaders.AUTHORIZATION)){
            List<String> accesstokens = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
            if(accesstokens != null && !accesstokens.isEmpty()){
                String accesstoken= accesstokens.get(0);
                if(accesstoken.contains("Bearer ")){
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if(!accesstoken.isEmpty()){
                        try{
                            JWT jwtToken = JWTParser.parse(accesstoken);
                            client_id = jwtToken.getJWTClaimsSet().getClaim("client_id").toString();
                        }
                        catch (ParseException e) {
                            return Mono.error(e);
                        }
                    }
                }

            }
        }
        return check(resourceId, client_id)
                .flatMap(condition -> {
                    if (condition) {
                        return chain.filter(exchange);
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resource"));

                    }
                }
                );

    }
    private Mono<Boolean> check(String resourceId, String clientId) {
        if(ALLOW_ALL_ACCESS.contains(resourceId)) return Mono.just(true);
        return repository.findByClientId(clientId)
            .switchIfEmpty(Mono.error(new Exception("Client Permissions not found")))
            .flatMap(clients -> {
                List resourceIds = clients.getResourceIds();
                if (resourceIds.contains(resourceId) & clients.getStatus().equals("Approved")) {
                    return Mono.just(true);
                } else {
                    return Mono.just(false);
                }
            });

    }
    @Override
    public int getOrder() {
        return 1;
    }
}
