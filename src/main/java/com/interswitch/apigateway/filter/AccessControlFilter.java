package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.util.ClientPermissionUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;


public class AccessControlFilter implements GlobalFilter, Ordered  {

    private ClientCacheRepository repository;

    private static List<String> ALLOW_ALL_ACCESS = Arrays.asList("passport-oauth");

    private ClientPermissionUtils util;

    public  AccessControlFilter(ClientCacheRepository repository, ClientPermissionUtils util) {
        this.util = util;
        this.repository = repository;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        HttpHeaders headers = exchange.getRequest().getHeaders();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String resourceId = (route != null) ? route.getId() : "";
        String client_id = util.GetClientIdFromBearerToken(headers);

        return check(resourceId, client_id)
                .flatMap(condition -> {
                    if (condition) {
                        return chain.filter(exchange);
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resource"));

                    }
                });
    }
    private Mono<Boolean> check(String resourceId, String clientId) {
        if(ALLOW_ALL_ACCESS.contains(resourceId)) return Mono.just(true);
        return repository.findByClientId(Mono.just(clientId))
            .switchIfEmpty(Mono.error(new Exception("Client Permissions not found")))
            .flatMap(client -> Mono.just(client.getResourceIds().contains(resourceId)));
    }
    @Override
    public int getOrder() {
        return 1;
    }
}
