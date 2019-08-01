package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.Trace;
import com.nimbusds.jwt.JWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetAddress;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;
import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.http.HttpStatus.Series.valueOf;

public class LoggingFilter implements WebFilter, Ordered {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public LoggingFilter() {
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        JWT token = decodeBearerToken(exchange.getRequest().getHeaders());
        String client_id = (token != null) ? getClaimAsStringFromBearerToken(token, "client_id") : "passport-client";
        Trace trace = new Trace();
        trace.setCallerIp(request.getRemoteAddress().getAddress().getHostAddress());
        trace.setCallerPort(request.getRemoteAddress().getPort());
        trace.setCallerName(request.getRemoteAddress().getAddress().getHostName());
        try {
            trace.setHostIp(InetAddress.getLocalHost().getHostAddress());
            trace.setHostName(InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            Mono.error(e).log();
        }

        trace.setHttpMethod(request.getMethodValue());
        trace.setHttpPath(request.getPath().toString());
        trace.setHttpRequestParams(request.getQueryParams().toString());
        trace.setClientId(client_id);

        return chain.filter(exchange)
                .doFinally((signalType) -> {
                    HttpStatus statusCode = exchange.getResponse().getStatusCode();
                    var status = (statusCode != null) ? valueOf(statusCode) : "nil";
                    Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
                    String routeId = (route != null) ? route.getId() : "nil";
                    String api = (route != null) ? route.getUri().toString() : "nil";
                    trace.setHttpUri(api);
                    trace.setRouteId(routeId);
                    trace.setHttpStatusCode(status.toString());

                    if (LOG.isInfoEnabled()) {
                        LOG.info("payload", value("trace", trace));
                    }
                });
    }

    @Override
    public int getOrder() {
        return -23456778;
    }
}