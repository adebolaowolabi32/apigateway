package com.interswitch.apigateway.filter;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.*;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

public class CorsFilter implements WebFilter, Ordered {

    private static final List<String> ALLOWED_HEADERS = Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
    private static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS);
    private static final List<String> VARY = Arrays.asList("Origin");
    private static final long MAX_AGE = 3600;
    private static final Boolean ALLOW_CREDENTIALS = true;
    private static final String ALLOWED_ORIGIN = "*";

    public CorsFilter() {}

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchangeDecorator decorator = new ServerWebExchangeDecoratorImpl(exchange);
        return chain.filter(decorator);
    }

    private class ServerWebExchangeDecoratorImpl extends ServerWebExchangeDecorator {

        private ServerHttpResponseDecorator responseDecorator;

        private ServerWebExchangeDecoratorImpl(ServerWebExchange delegate) {
            super(delegate);
            this.responseDecorator = new ServerHttpResponseDecoratorImpl(delegate.getResponse());
        }

        @Override
        public ServerHttpResponseDecorator getResponse() {
            return responseDecorator;
        }

    }

    private class ServerHttpResponseDecoratorImpl extends ServerHttpResponseDecorator {

        private ServerHttpResponse response;

        private ServerHttpResponseDecoratorImpl(ServerHttpResponse response) {
            super(response);
            this.response = response;
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders headers = reduceHeaders(response.getHeaders());
            headers.setVary(VARY);
            headers.setAccessControlAllowOrigin(ALLOWED_ORIGIN);
            headers.setAccessControlAllowHeaders(ALLOWED_HEADERS);
            headers.setAccessControlAllowMethods(ALLOWED_METHODS);
            headers.setAccessControlAllowCredentials(ALLOW_CREDENTIALS);
            headers.setAccessControlMaxAge(MAX_AGE);
            return headers;
        }

        private HttpHeaders reduceHeaders(HttpHeaders headers){
            for (String key : headers.keySet()) {
                List<String> headerValue = (headers.get(key) != null) ? headers.get(key) : Collections.emptyList();
                String newValue = headerValue != null ? headerValue.get(0) != null ? headerValue.get(0) : "" : "";
                headers.replace(key, Collections.singletonList(newValue));
            }
            return headers;
        }
    }
    @Override
    public int getOrder() {
        return 0;
    }
}

