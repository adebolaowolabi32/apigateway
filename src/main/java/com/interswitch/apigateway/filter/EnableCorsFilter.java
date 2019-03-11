package com.interswitch.apigateway.filter;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.fabric8.kubernetes.api.model.apiextensions.JSONBuilder;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class EnableCorsFilter implements WebFilter, Ordered {

    private static final List<String> ALLOWED_HEADERS = Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
    private static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS);
    private static List<String> ALLOWED_ORIGIN;

    private static final long MAX_AGE = 3600;
    private static final Boolean ALLOW_CREDENTIALS = true;
    String clientId = "";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders requestHeaders = request.getHeaders();
            HttpHeaders responseHeaders = response.getHeaders();

            //TODO decode jwt
        if(requestHeaders.containsKey("Authorization")){
            List<String> accesstoken = request.getHeaders().get("Authorization");
            if (accesstoken != null && !accesstoken.isEmpty()) {
                String accesstokenEncodedValue = accesstoken.get(0).replaceFirst("Bearer ", "");

                if(!accesstokenEncodedValue.isEmpty()){
                    try {
                        JWT jwtToken = JWTParser.parse(accesstokenEncodedValue);
                        clientId = jwtToken.getJWTClaimsSet().getClaim("client_id").toString();
                        System.out.println("ClientID: "+ clientId);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                        return null;
                    }
                }
            }

        }
        //fetch origin from request, compare with list of alwdogns
            //loop thru listof allowed orgins set if match, if not match set


        responseHeaders.setAccessControlAllowOrigin(clientId);
        responseHeaders.setAccessControlAllowHeaders(ALLOWED_HEADERS);
        responseHeaders.setAccessControlAllowMethods(ALLOWED_METHODS);
        responseHeaders.setAccessControlAllowCredentials(ALLOW_CREDENTIALS);
        responseHeaders.setAccessControlMaxAge(MAX_AGE);

            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
            }
            return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
