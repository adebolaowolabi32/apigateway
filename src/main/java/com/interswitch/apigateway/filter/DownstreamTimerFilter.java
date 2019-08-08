package com.interswitch.apigateway.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class DownstreamTimerFilter implements GlobalFilter, Ordered {
    public static final String DOWNSTREAM_ROUTE_DURATION = "downstream_route_duration";

    private final MeterRegistry meterRegistry;

    public DownstreamTimerFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Timer.Sample sample = Timer.start(this.meterRegistry);
        return chain.filter(exchange).doOnSuccessOrError((sc, ex) -> {
            long duration = sample.stop(this.meterRegistry.timer("route.duration"));
            exchange.getAttributes().put(DOWNSTREAM_ROUTE_DURATION, duration);
        });
    }

    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
