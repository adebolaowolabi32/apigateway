package com.interswitch.apigateway.util;

import com.interswitch.apigateway.handler.RouteHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.ControllerEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

public class RouteUtil {
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private ControllerEndpointHandlerMapping controllerEndpointHandlerMapping;

    private WebFluxEndpointHandlerMapping webFluxEndpointHandlerMapping;

    private RouteHandlerMapping routeHandlerMapping;

    private SecurityUtil securityUtil;

    public RouteUtil(RequestMappingHandlerMapping requestMappingHandlerMapping, ControllerEndpointHandlerMapping controllerEndpointHandlerMapping, WebFluxEndpointHandlerMapping webFluxEndpointHandlerMapping, RouteHandlerMapping routeHandlerMapping, SecurityUtil securityUtil) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.controllerEndpointHandlerMapping = controllerEndpointHandlerMapping;
        this.webFluxEndpointHandlerMapping = webFluxEndpointHandlerMapping;
        this.routeHandlerMapping = routeHandlerMapping;
        this.securityUtil = securityUtil;
    }

    public Mono<Boolean> isRouteBasedEndpoint(ServerWebExchange exchange) {
        return isInternalEndpoint(exchange).flatMap(isInternalEndpoint ->
                this.isGatewayEndpoint(exchange).flatMap(isGatewayEndpoint ->
                        this.isActuatorEndpoint((exchange)).flatMap(isActuatorEndpoint ->
                            Mono.just(!(isInternalEndpoint || isGatewayEndpoint || isActuatorEndpoint)))));
    }

    public Mono<Boolean> isActuatorEndpoint(ServerWebExchange exchange) {
        return webFluxEndpointHandlerMapping.getHandlerInternal(exchange).hasElement();
    }

    public Mono<Boolean> isInternalEndpoint(ServerWebExchange exchange) {
        return requestMappingHandlerMapping.getHandlerInternal(exchange).hasElement();
    }

    public Mono<Boolean> isGatewayEndpoint(ServerWebExchange exchange) {
        return controllerEndpointHandlerMapping.getHandlerInternal(exchange).hasElement();
    }

    public Mono<AtomicBoolean> isRequestAuthenticated(ServerWebExchange exchange) {
        return routeHandlerMapping.lookupRoute(exchange).flatMap(route ->
                securityUtil.isRequestAuthenticated(route, exchange)).switchIfEmpty(Mono.just(new AtomicBoolean(false)));
    }
}
