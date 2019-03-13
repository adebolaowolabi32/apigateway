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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorsFilter implements WebFilter, Ordered {

    static final List<String> ALLOWED_HEADERS = Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
    static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS);
    static final long MAX_AGE = 3600;
    static final Boolean ALLOW_CREDENTIALS = true;
    static String ALLOWED_ORIGIN = "";
    static List<String> URLS_TO_ALLOW_ALL_ORIGINS = Arrays.asList("/passport/oauth/token", "/passport/oauth/authorize");


    @Autowired
    private ClientCacheRepository clientCacheRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ALLOWED_ORIGIN = "";
        String clientId = "";
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders requestHeaders = request.getHeaders();
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
        return clientCacheRepository.findByClientId(clientId).flatMap(client -> {
            List<String> origins = client.getOrigins();
            if (origins.contains(requestOrigin))
                ALLOWED_ORIGIN = requestOrigin;
            return Mono.empty();
        }).then(Mono.defer(() -> {
                if (URLS_TO_ALLOW_ALL_ORIGINS.contains(path)) ALLOWED_ORIGIN = "*";
                if (request.getMethod().equals(HttpMethod.OPTIONS)) {
                    ALLOWED_ORIGIN = requestOrigin;
                    response.setStatusCode(HttpStatus.OK);
                }

                if (ALLOWED_ORIGIN == null || ALLOWED_ORIGIN.isEmpty()) {
                    if (requestOrigin == null) ALLOWED_ORIGIN = "No Origin Header Present";
                    else if (requestOrigin.trim().isEmpty()) ALLOWED_ORIGIN = "Origin Header is Empty";
                    else ALLOWED_ORIGIN = "Origin is not Allowed";
                }

                ServerWebExchangeDecorator decorator = new ServerWebExchangeDecoratorImpl(exchange);
                return chain.filter(decorator);
            }));
    }

    @Override
    public int getOrder() {
        return 1;
    }

    class ServerWebExchangeDecoratorImpl extends ServerWebExchangeDecorator {

        private ServerHttpResponseDecorator responseDecorator;

        public ServerWebExchangeDecoratorImpl(ServerWebExchange delegate) {
            super(delegate);
            this.responseDecorator = new ServerHttpResponseDecoratorImpl(delegate.getResponse());
        }

        @Override
        public ServerHttpResponseDecorator getResponse() {
            return responseDecorator;
        }

    }

    class ServerHttpResponseDecoratorImpl extends ServerHttpResponseDecorator {

        private HttpHeaders headers;
        private ServerHttpResponse response;

        public ServerHttpResponseDecoratorImpl(ServerHttpResponse response) {
            super(response);
            this.response = response;
        }

        @Override
        public HttpHeaders getHeaders() {
            headers = reduceHeaders(response.getHeaders());
            headers.setVary(Arrays.asList("Origin"));
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
                String newValue = "";

                for (String value : headerValue) {
                    newValue = value;
                    break;
                }
                headers.replace(key, Arrays.asList(newValue));
            }
            return headers;
        }
    }

}

