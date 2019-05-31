package com.interswitch.apigateway.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.sleuth.instrument.web.TraceWebFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class LoggingFilter implements GlobalFilter, Ordered {
    protected static final String TRACE_REQUEST_ATTR = TraceWebFilter.class.getName()
            + ".TRACE";
    @Override
    public int getOrder() {
        return -23456778;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Log log = LogFactory.getLog(getClass());
        String traceId = exchange.getAttribute(TRACE_REQUEST_ATTR).toString();
        log.trace("Incoming Request: "+exchange.getRequest().getId()+ " TraceId: "+traceId+" Path: "+exchange.getRequest().getPath()+
                        " Method: "+exchange.getRequest().getMethodValue()+
                " Headers: "+exchange.getRequest().getHeaders().values());
        return chain.filter(exchange);
    }
}