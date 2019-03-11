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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class EnableCorsFilter implements WebFilter, Ordered {

    private static final List<String> ALLOWED_HEADERS = Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
    private static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS);
    private static String ALLOWED_ORIGIN = "";
    private static final long MAX_AGE = 3600;
    private static final Boolean ALLOW_CREDENTIALS = true;

    @Autowired
    private ClientCacheRepository clientCacheRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders requestHeaders = request.getHeaders();
        HttpHeaders responseHeaders = response.getHeaders();
        String requestOrigin = requestHeaders.getOrigin();
        ALLOWED_ORIGIN = "";
        String clientId = "";

        if(requestHeaders.containsKey("Authorization") ){
            List<String> accesstoken = request.getHeaders().get("Authorization");
            if (accesstoken != null && !accesstoken.isEmpty()) {
                String accesstokenEncodedValue = accesstoken.get(0).replaceFirst("Bearer ", "");
                if(!accesstokenEncodedValue.isEmpty()){
                    try {
                        JWT jwtToken = JWTParser.parse(accesstokenEncodedValue);
                        clientId = jwtToken.getJWTClaimsSet().getClaim("client_id").toString();
                    }
                    catch (ParseException ex) {
                        return Mono.error(ex);
                    }
                }
            }
        }

        return clientCacheRepository.findByClientId(clientId).flatMap(client -> {
                List<String> origins = client.getOrigins();
                if(origins.contains(requestOrigin))
                    ALLOWED_ORIGIN = requestOrigin;
                return Mono.empty();
            })
           .then(Mono.defer(() -> {
               if(request.getMethod().equals(HttpMethod.OPTIONS)){
                   ALLOWED_ORIGIN = requestOrigin;
                   response.setStatusCode(HttpStatus.OK);
               }

               if (ALLOWED_ORIGIN == null || ALLOWED_ORIGIN.isEmpty()){
                   if(requestOrigin == null) ALLOWED_ORIGIN = "No Origin Header Present";
                   else if(requestOrigin.trim().isEmpty()) ALLOWED_ORIGIN = "Origin Header is Empty";
                   else ALLOWED_ORIGIN = "Origin is not Allowed";
               }

                responseHeaders.setAccessControlAllowOrigin(ALLOWED_ORIGIN);
                responseHeaders.setAccessControlAllowHeaders(ALLOWED_HEADERS);
                responseHeaders.setAccessControlAllowMethods(ALLOWED_METHODS);
                responseHeaders.setAccessControlAllowCredentials(ALLOW_CREDENTIALS);
                responseHeaders.setAccessControlMaxAge(MAX_AGE);
                return chain.filter(exchange);
            }));
}

    @Override
    public int getOrder() {
        return 1;
    }

}
