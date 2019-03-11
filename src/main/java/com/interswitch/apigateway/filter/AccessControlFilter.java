package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.ClientResourcesRepository;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


public class AccessControlFilter implements GlobalFilter, Ordered  {

    private ClientResourcesRepository repository;

    public  AccessControlFilter(ClientResourcesRepository repository) {
        this.repository=repository;

    }
    String client_id = "";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String accessToken = exchange.getRequest().getHeaders().get("Authorization").toString();
        accessToken = accessToken.replace(accessToken.substring(accessToken.indexOf("B") - 1, accessToken.indexOf(" ") + 1), "");
        String resourceId = (exchange.getRequest().getMethod().toString()) + (exchange.getRequest().getPath().toString());

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setExpectedAudience("isw-core")
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .setJwsAlgorithmConstraints(
                        new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
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
            return check(resourceId)
                    .flatMap(condition -> {
                                if (condition.equals(true)) {
                                    return chain.filter(exchange);
                                } else {
                                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resource"));

                                }
                            }
                    );

    }
    private Mono<Boolean> check(String resourceId) {
            return repository.findByClientId(client_id)
                .switchIfEmpty(Mono.error(new Exception()))
                .flatMap(clientResources -> {
                    List resourceIds = clientResources.getResourceIds();
                    if (resourceIds.contains(resourceId)) {
                        return Mono.just(true);
                    } else {
                        return Mono.just(false);
                    }
                });

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
