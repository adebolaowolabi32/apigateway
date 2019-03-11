package com.interswitch.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoveDuplicateHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                List<String> headerValue = headers.get(key);
                StringBuilder stringBuilder = new StringBuilder();
                Boolean isAllowOrigin = false;

                if(key.equalsIgnoreCase("Access-Control-Allow-Origin")){
                    isAllowOrigin = true;
                }

                Boolean isFirst = true;

                for (String value : headerValue) {
                    if(isFirst){
                        stringBuilder.append(value);
                        isFirst = false;
                    }
                    else {
                        if(!isAllowOrigin)
                        stringBuilder.append(",").append(value);
                    }
                }
                List<String> listOfValues =  Arrays.asList(stringBuilder.toString().split(","));

                headers.replace(key, Arrays.asList(listOfValues.stream()
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.joining(","))));
            }
        }));
    }

    @Override
    public int getOrder() {
        return 1000;
    }

}
