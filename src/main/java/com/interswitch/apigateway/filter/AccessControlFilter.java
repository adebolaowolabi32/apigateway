package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.MongoClientRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;


public class AccessControlFilter implements GlobalFilter, Ordered  {

    private MongoClientRepository repository;

    private static List<String> PERMIT_ALL = Collections.singletonList("passport");

    public  AccessControlFilter(MongoClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        HttpHeaders headers = exchange.getRequest().getHeaders();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String routeId = (route != null) ? route.getId() : "";

        if(!routeId.isBlank())
            if(PERMIT_ALL.contains(routeId))
                return chain.filter(exchange);

        String clientId = GetClientIdFromBearerToken(headers);
        List<String> resources = GetResourcesFromBearerToken(headers);

        return repository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Client not found")))
                .flatMap(clients -> {
                    for(var r : resources) {
                        int indexOfFirstSlash = r.indexOf('/');
                        String method = r.substring(0, indexOfFirstSlash);
                        String path = r.substring(indexOfFirstSlash);
                        if(exchange.getRequest().getPath().toString().equals(path))
                            if (exchange.getRequest().getMethodValue().equals(method))
                                return chain.filter(exchange);
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resource"));
                });
    }

    public String GetClientIdFromBearerToken(HttpHeaders headers) {
        String client_id = "";
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokens = headers.get(HttpHeaders.AUTHORIZATION);
            if (accesstokens != null && !accesstokens.isEmpty()) {
                String accesstoken = accesstokens.get(0);
                if (accesstoken.contains("Bearer ")) {
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if (!accesstoken.isEmpty()) {
                        try {
                            JWT jwtToken = JWTParser.parse(accesstoken);
                            client_id = jwtToken.getJWTClaimsSet().getClaim("client_id").toString();
                        } catch (ParseException e) {
                            Mono.error(e).log();
                        }
                    }
                }
            }
        }
        return client_id;
    }

    public List<String> GetResourcesFromBearerToken(HttpHeaders headers) {
        List<String> resources = new ArrayList<>();
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokens = headers.get(HttpHeaders.AUTHORIZATION);
            if (accesstokens != null && !accesstokens.isEmpty()) {
                String accesstoken = accesstokens.get(0);
                if (accesstoken.contains("Bearer ")) {
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if (!accesstoken.isEmpty()) {
                        try {
                            JWT jwtToken = JWTParser.parse(accesstoken);
                            resources = (List<String>)jwtToken.getJWTClaimsSet().getClaim("api_resources");
                        } catch (ParseException e) {
                            Mono.error(e).log();
                        }
                    }
                }
            }
        }
        return resources;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
