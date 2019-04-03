package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.util.ClientPermissionUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.*;

public class CorsFilter implements WebFilter, Ordered {

    private static final List<String> ALLOWED_HEADERS = Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
    private static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS);
    private static final long MAX_AGE = 3600;
    private static final Boolean ALLOW_CREDENTIALS = true;
    private static String ALLOWED_ORIGIN = "";
    private static List<String> ALLOW_FOR_ALL_ORIGINS = Arrays.asList("/passport/oauth/token", "/passport/oauth/authorize");

    private ClientCacheRepository clientCacheRepository;

    private ClientPermissionUtils util;

    private ServerHttpRequest request;
    private ServerHttpResponse response;

    public CorsFilter(ClientCacheRepository clientCacheRepository, ClientPermissionUtils util) {
        this.util = util;
        this.clientCacheRepository = clientCacheRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        request = exchange.getRequest();
        response = exchange.getResponse();
        ALLOWED_ORIGIN = "";
        String requestOrigin = request.getHeaders().getOrigin();
        HttpMethod requestMethod = request.getMethod();

        String clientId = util.GetClientIdFromBearerToken(request.getHeaders());

        return clientCacheRepository.findByClientId(Mono.just(clientId))
                .flatMap(client -> {
                    if (client.getOrigins().contains(requestOrigin))
                        ALLOWED_ORIGIN = requestOrigin;
                    return Mono.empty();
                })
                .then(Mono.defer(() -> {
                    if (requestMethod != null && requestMethod.equals(HttpMethod.OPTIONS)) {
                        response.setStatusCode(HttpStatus.OK);
                        ALLOWED_ORIGIN = requestOrigin;
                    }
                    if (ALLOW_FOR_ALL_ORIGINS.contains(request.getURI().getPath())){
                        ALLOWED_ORIGIN = "*";
                    }
                    if (ALLOWED_ORIGIN == null || ALLOWED_ORIGIN.isEmpty()) {
                        if (requestOrigin == null) ALLOWED_ORIGIN = "No-Origin-Header-Present";
                        else if (requestOrigin.trim().isEmpty()) ALLOWED_ORIGIN = "Origin-Header-is-Empty";
                        else ALLOWED_ORIGIN = "Origin-is-not-Allowed";
                    }
                    ServerWebExchangeDecorator decorator = new ServerWebExchangeDecoratorImpl(exchange);
                    return chain.filter(decorator);
                }));

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

        private HttpHeaders headers;
        private ServerHttpResponse response;

        private ServerHttpResponseDecoratorImpl(ServerHttpResponse response) {
            super(response);
            this.response = response;
        }

        @Override
        public HttpHeaders getHeaders() {
            headers = reduceHeaders(response.getHeaders());
            headers.setVary(Collections.singletonList("Origin"));
            headers.setAccessControlAllowOrigin(ALLOWED_ORIGIN);
            headers.setAccessControlAllowHeaders(ALLOWED_HEADERS);
            headers.setAccessControlAllowMethods(ALLOWED_METHODS);
            headers.setAccessControlAllowCredentials(ALLOW_CREDENTIALS);
            headers.setAccessControlMaxAge(MAX_AGE);
            return headers;
        }

        private HttpHeaders reduceHeaders(HttpHeaders headers){
            for (String key : headers.keySet()) {
                List<String> headerValue = (headers.get(key) != null) ? headers.get(key) : new ArrayList();
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

