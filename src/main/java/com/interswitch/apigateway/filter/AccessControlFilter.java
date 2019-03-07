package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.ClientResourcesRepository;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


public class AccessControlFilter implements GlobalFilter, Ordered  {
    @Value("${spring.security.oauth2.resourceserver.jwt.keyValue}")
    private String key;

    private ClientResourcesRepository repository;

    public  AccessControlFilter(ClientResourcesRepository repository) {
        this.repository=repository;

    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String client_id = "";
        String accessToken = exchange.getRequest().getHeaders().get("Authorization").toString();
        accessToken = accessToken.replace(accessToken.substring(accessToken.indexOf("B") - 1, accessToken.indexOf(" ") + 1), "");
        String resourceId = (exchange.getRequest().getMethod().toString()) + (exchange.getRequest().getPath().toString());

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime() // the JWT must have an expiration time
                .setAllowedClockSkewInSeconds(30)// allow some leeway in validating time based claims to account for clock skew
                .setExpectedAudience("isw-core")
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .setJwsAlgorithmConstraints( // only allow the expected signature algorithm(s) in the given context
                        new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, // which is only RS256 here
                                AlgorithmIdentifiers.RSA_USING_SHA256))
                .build();

        try {
            client_id = jwtConsumer.processToClaims(accessToken).getStringClaimValue("client_id");
        } catch (MalformedClaimException e) {
            e.printStackTrace();
        } catch (InvalidJwtException e) {
            e.printStackTrace();
            try {
                client_id = e.getJwtContext().getJwtClaims().getStringClaimValue("client_id");
            } catch (MalformedClaimException e1) {
                e1.printStackTrace();
            }
        }
        return repository.findByClientId(client_id)
                .flatMap(item -> {
                    List resourceIds = item.getResourceIds();
                    if (resourceIds.contains(resourceId)) {
                        return chain.filter(exchange.mutate().build());
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,"You are not allowed to use this resource"));
                    }
                })
//                .doOnSuccess(c -> chain.filter(exchange))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "You do not have access to this resource")));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
