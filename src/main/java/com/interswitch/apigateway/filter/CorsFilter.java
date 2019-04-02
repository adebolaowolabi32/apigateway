package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.text.ParseException;
import java.util.*;

public class CorsFilter implements WebFilter, Ordered {

    private static final List<String> ALLOWED_HEADERS = Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
    private static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS);
    private static final long MAX_AGE = 3600;
    private static final Boolean ALLOW_CREDENTIALS = true;
    private static String ALLOWED_ORIGIN = "";
    private static List<String> URLS_TO_ALLOW_ALL_ORIGINS = Arrays.asList("/passport/oauth/token", "/passport/oauth/authorize");


    @Autowired
    private ClientCacheRepository clientCacheRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ALLOWED_ORIGIN = "";
        String clientId = "";
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders requestHeaders = request.getHeaders();
        HttpMethod requestMethod = request.getMethod();
        String requestOrigin = requestHeaders.getOrigin();
        String path = request.getURI().getPath();

        if (requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokenl = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (accesstokenl != null && !accesstokenl.isEmpty()) {
                String accesstoken = accesstokenl.get(0);
                if (accesstoken.contains("Bearer ")) {
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if (!accesstoken.isEmpty()) {
                        try {
                            JWT jwtToken = JWTParser.parse(accesstoken);
                            clientId = jwtToken.getJWTClaimsSet().getClaim("client_id").toString();
                        } catch (ParseException ex) {
                            return Mono.error(ex);
                        }
                    }
                }
            }
        }
        return clientCacheRepository.findByClientId(Mono.just(clientId)).flatMap(client -> {
            List<String> origins = client.getOrigins();
            if (origins.contains(requestOrigin))
                ALLOWED_ORIGIN = requestOrigin;
            return Mono.empty();
        }).then(Mono.defer(() -> {
                if (requestMethod != null && requestMethod.equals(HttpMethod.OPTIONS)) {
                    ALLOWED_ORIGIN = requestOrigin;
                    response.setStatusCode(HttpStatus.OK);
                }
                if (URLS_TO_ALLOW_ALL_ORIGINS.contains(path)) ALLOWED_ORIGIN = "*";
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

