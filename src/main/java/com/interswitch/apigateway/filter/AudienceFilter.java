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
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        List<String> audience = GetAudienceFromBearerToken(exchange.getRequest().getHeaders());
        if(audience.contains("api-gateway"))
            return chain.filter(exchange);
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient rights"));
    }

    public List<String> GetAudienceFromBearerToken(HttpHeaders headers) {
        List<String> audience = new ArrayList<>();
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokens = headers.get(HttpHeaders.AUTHORIZATION);
            if (accesstokens != null && !accesstokens.isEmpty()) {
                String accesstoken = accesstokens.get(0);
                if (accesstoken.contains("Bearer ")) {
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if (!accesstoken.isEmpty()) {
                        try {
                            JWT jwtToken = JWTParser.parse(accesstoken);
                            audience = (List<String>)jwtToken.getJWTClaimsSet().getClaim("aud");
                        } catch (ParseException e) {
                            Mono.error(e).log();
                        }
                    }
                }
            }
        }
        return audience;
    }

    @Override
    public int getOrder() {
        return -33456778;
    }
}
