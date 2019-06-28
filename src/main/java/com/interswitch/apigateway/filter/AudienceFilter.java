package com.interswitch.apigateway.filter;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class AudienceFilter implements WebFilter, Ordered {
    private static String PassportRoute= "/passport/oauth/token";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        JWT token = DecodeBearerToken(exchange.getRequest().getHeaders());
        if(token!=null) {
            String exchangePath = exchange.getRequest().getPath().toString();
            List<String> audience = GetAudience(token);
            String environment = GetEnvironment(token);
            if (audience.contains("api-gateway") & environment == "TEST" & exchangePath == PassportRoute)
                return chain.filter(exchange);
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have sufficient rights to this resource"));
        }
        else{
            return chain.filter(exchange);
        }
    }

    public JWT DecodeBearerToken(HttpHeaders headers) {
        JWT jwtToken= null;
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokens = headers.get(HttpHeaders.AUTHORIZATION);
            if (accesstokens != null && !accesstokens.isEmpty()) {
                String accesstoken = accesstokens.get(0);
                if (accesstoken.contains("Bearer ")) {
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if (!accesstoken.isEmpty()) {
                        try {
                            jwtToken = JWTParser.parse(accesstoken);
                        } catch (ParseException e) {
                            Mono.error(e).log();
                        }
                    }
                }
            }
        }
        return jwtToken ;
    }
    public List<String> GetAudience(JWT jwtToken){
        List<String> audience = new ArrayList<>();
        try {
            Object aud = jwtToken.getJWTClaimsSet().getClaim("aud");
            if (aud != null) audience = (List<String>) aud;
        }
        catch (ParseException e){
            Mono.error(e).log();
        }
        return audience;

    }

    public String GetEnvironment(JWT jwtToken){
        String environment = "";
        try {
            Object env = jwtToken.getJWTClaimsSet().getClaim("env");
            if (env != null) environment = env.toString();
            return environment;
        }
        catch (ParseException e){
            Mono.error(e).log();
        }
        return environment;


    }

    @Override
    public int getOrder() {
        return -33456778;
    }
}
