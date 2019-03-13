package com.interswitch.apigateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class RouteIdFilter implements WebFilter, Ordered {

    String GATEWAY_SAVE_URL = "/actuator/gateway/routes/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String fullPath = req.getURI().getPath();
        int indexOfLastSlash = fullPath.lastIndexOf('/') + 1;
        String path = fullPath.substring(0, indexOfLastSlash);

        if(req.getMethod() == HttpMethod.POST && path.equalsIgnoreCase(GATEWAY_SAVE_URL)){

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(path);

            String id = fullPath.substring(indexOfLastSlash);

            if(!id.isEmpty()){
                String unique = UUID.randomUUID().toString().replaceAll("-", "");
                stringBuilder.append(id);
                stringBuilder.append(":");
                stringBuilder.append(unique);
            }
            fullPath = stringBuilder.toString();
        }

        ServerHttpRequest request = req.mutate()
                .path(fullPath)
                .build();

        return chain.filter(exchange.mutate().request(request).build());

    }
    @Override
    public int getOrder() {
        return 1000;
    }
}
