package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.Env;
import com.nimbusds.jwt.JWT;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;

public class ResponseInterceptor implements WebFilter, Ordered {

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        JWT token = decodeBearerToken(exchange.getRequest().getHeaders());
        String env = getClaimAsStringFromBearerToken(token, "env");
        if (!env.equalsIgnoreCase(Env.TEST.toString()))
            return chain.filter(exchange);

        ServerHttpResponse response = exchange.getResponse();
        return chain.filter(exchange.mutate().response(new ServerHttpResponseDecorator(response) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body != null) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                        return super.writeWith(fluxBody.map(dataBuffer -> {
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            String message = new String(content, Charset.forName("UTF-8"));
                            if (response.getStatusCode().equals(HttpStatus.FORBIDDEN) || message.contains("Invalid token does not contain resource id"))
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your access token is no longer valid, kindly refresh your credentials on developer console");

                            return response.bufferFactory().wrap(content);
                        }));
                    }
                }
                return super.writeWith(body);
            }
        }).build());
    }

    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}