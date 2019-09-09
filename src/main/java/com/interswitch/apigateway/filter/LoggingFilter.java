package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.Trace;
import com.nimbusds.jwt.JWT;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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

import static com.interswitch.apigateway.filter.DownstreamTimerFilter.DOWNSTREAM_ROUTE_DURATION;
import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;
import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.http.HttpStatus.Series.valueOf;

public class LoggingFilter implements WebFilter, Ordered {

    private final MeterRegistry meterRegistry;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public LoggingFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Timer.Sample sample = Timer.start(this.meterRegistry);

        ServerHttpRequest request = exchange.getRequest();
        JWT token = decodeBearerToken(exchange.getRequest().getHeaders());
        String client_id = getClaimAsStringFromBearerToken(token, "client_id");
        Trace trace = new Trace();
        if (request.getRemoteAddress() != null) {
            trace.setCallerIp(request.getRemoteAddress().getAddress().getHostAddress());
            trace.setCallerPort(request.getRemoteAddress().getPort());
            trace.setCallerName(request.getRemoteAddress().getAddress().getHostName());
        }

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
                    long totalRequestDuration = sample.stop(this.meterRegistry.timer("gateway.request.duration"));

                    HttpStatus statusCode = exchange.getResponse().getStatusCode();
                    var status = (statusCode != null) ? valueOf(statusCode) : "No Status Code";
                    Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
                    String routeId = (route != null) ? route.getId() : "API Gateway";
                    String api = (route != null) ? route.getUri().toString() : "API Gateway";
                    Long duration = exchange.getAttribute(DOWNSTREAM_ROUTE_DURATION);
                    long downstreamRouteDuration = duration != null ? duration : 0;

                    trace.setHttpUri(api);
                    trace.setRouteId(routeId);
                    trace.setHttpStatusCode(status.toString());
                    trace.setRequestDuration(totalRequestDuration / 1000000);
                    trace.setDurationOutsideGateway(downstreamRouteDuration / 1000000);
                    trace.setDurationWithinGateway((totalRequestDuration - downstreamRouteDuration) / 1000000);

                    if (LOG.isInfoEnabled()) {
                        LOG.info("payload", value("trace", trace));
                    }
                });
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}