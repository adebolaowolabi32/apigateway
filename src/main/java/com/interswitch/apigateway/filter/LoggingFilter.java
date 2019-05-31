package com.interswitch.apigateway.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.sleuth.instrument.web.TraceWebFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

public class LoggingFilter implements GlobalFilter, Ordered {
    protected static final String TRACE_REQUEST_ATTR = TraceWebFilter.class.getName()
            + ".TRACE";
    protected static final Log log = LogFactory.getLog(LoggingFilter.class);

    @Override
    public int getOrder() {
        return -23456778;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getAttribute(TRACE_REQUEST_ATTR).toString();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String originalUri=route.getUri().toString();
        String message= "Incoming Request: "+exchange.getRequest().getId()+" TraceId: "+traceId+"To: "+originalUri+" Path: "+exchange.getRequest().getPath()+
                " Method: "+exchange.getRequest().getMethodValue()+
                " Headers: "+exchange.getRequest().getHeaders().values();
        if (log.isDebugEnabled()) {
        log.debug(message);}
        return chain.filter(exchange);
    }
}