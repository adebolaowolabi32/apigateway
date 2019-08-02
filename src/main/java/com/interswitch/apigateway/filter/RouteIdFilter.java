package com.interswitch.apigateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RouteIdFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String fullPath = request.getURI().getPath();
        int indexOfLastSlash = fullPath.lastIndexOf('/') + 1;
        String path = fullPath.substring(0, indexOfLastSlash);
        String GATEWAY_SAVE_URL = "/actuator/gateway/routes/";
        List<String> passportRoutes = Collections.singletonList("passport");


        if(request.getMethod() == HttpMethod.POST && path.equalsIgnoreCase(GATEWAY_SAVE_URL)){
            String id = fullPath.substring(indexOfLastSlash);

            if(!id.isEmpty() && !id.contains(":") && !passportRoutes.contains(id)){
                String unique = UUID.randomUUID().toString().replaceAll("-", "");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(path);
                stringBuilder.append(id);
                stringBuilder.append(":");
                stringBuilder.append(unique);

                request = request.mutate()
                        .path(stringBuilder.toString())
                        .build();
            }
        }

        return chain.filter(exchange.mutate().request(request).build());

    }
    @Override
    public int getOrder() {
        return -50;
    }
}
