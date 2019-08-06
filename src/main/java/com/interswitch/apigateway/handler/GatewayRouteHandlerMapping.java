package com.interswitch.apigateway.handler;

import com.interswitch.apigateway.repository.MongoRouteConfigRepository;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping.ManagementPortType.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

@Component
public class GatewayRouteHandlerMapping extends AbstractHandlerMapping {

    private final FilteringWebHandler webHandler;

    private final RouteLocator routeLocator;

    private final Integer managementPort;

    private final RoutePredicateHandlerMapping.ManagementPortType managementPortType;

    private MongoRouteConfigRepository repository;

    public GatewayRouteHandlerMapping(FilteringWebHandler webHandler,
                                      RouteLocator routeLocator, GlobalCorsProperties globalCorsProperties,
                                      Environment environment, MongoRouteConfigRepository repository) {
        this.webHandler = webHandler;
        this.routeLocator = routeLocator;

        this.managementPort = getPortProperty(environment, "management.server.");
        this.managementPortType = getManagementPortType(environment);
        this.repository = repository;
        setOrder(1);
        setCorsConfigurations(globalCorsProperties.getCorsConfigurations());
    }

    private static Integer getPortProperty(Environment environment, String prefix) {
        return environment.getProperty(prefix + "port", Integer.class);
    }

    private RoutePredicateHandlerMapping.ManagementPortType getManagementPortType(Environment environment) {
        Integer serverPort = getPortProperty(environment, "server.");
        if (this.managementPort != null && this.managementPort < 0) {
            return DISABLED;
        }
        return ((this.managementPort == null
                || (serverPort == null && this.managementPort.equals(8080))
                || (this.managementPort != 0 && this.managementPort.equals(serverPort)))
                ? SAME : DIFFERENT);
    }

    @Override
    protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {
        // don't handle requests on management port if set and different than server port
        if (this.managementPortType == DIFFERENT && this.managementPort != null
                && exchange.getRequest().getURI().getPort() == this.managementPort) {
            return Mono.empty();
        }
        exchange.getAttributes().put(GATEWAY_HANDLER_MAPPER_ATTR, getSimpleName());

        return lookupRoute(exchange)
                // .log("route-predicate-handler-mapping", Level.FINER) //name this
                .flatMap((Function<Route, Mono<?>>) r -> {
                    exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Mapping [" + getExchangeDesc(exchange) + "] to " + r);
                    }

                    exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, r);
                    return Mono.just(webHandler);
                }).switchIfEmpty(Mono.empty().then(Mono.fromRunnable(() -> {
                    exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
                    if (logger.isTraceEnabled()) {
                        logger.trace("No RouteDefinition found for ["
                                + getExchangeDesc(exchange) + "]");
                    }
                })));
    }

    @Override
    protected CorsConfiguration getCorsConfiguration(Object handler,
                                                     ServerWebExchange exchange) {
        // TODO: support cors configuration via properties on a route see gh-229
        // see RequestMappingHandlerMapping.initCorsConfiguration()
        // also see
        // https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/test/java/org/springframework/web/cors/reactive/CorsWebFilterTests.java
        return super.getCorsConfiguration(handler, exchange);
    }

    // TODO: get desc from factory?
    private String getExchangeDesc(ServerWebExchange exchange) {
        StringBuilder out = new StringBuilder();
        out.append("Exchange: ");
        out.append(exchange.getRequest().getMethod());
        out.append(" ");
        out.append(exchange.getRequest().getURI());
        return out.toString();
    }

    protected Mono<Route> lookupRoute(ServerWebExchange exchange) {
        return this.routeLocator.getRoutes()
                .concatMap(route -> Mono.just(route).filterWhen(r -> {
                    exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, r.getId());
                    return r.getPredicate().apply(exchange);
                })
                        .doOnError(e -> logger.error(
                                "Error applying predicate for route: " + route.getId(),
                                e))
                        .onErrorResume(e -> Mono.empty()))
                .next()
                // TODO: error handling
                .map(route -> {
//
//                    route =Route.async().id(route.getId()).uri(uri).order(0).asyncPredicate(route.getPredicate())
//                            .build();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Route matched: " + route.getId());
                    }

                    validateRoute(route, exchange);
                    return route;
                });

        /*
         * TODO: trace logging if (logger.isTraceEnabled()) {
         * logger.trace("RouteDefinition did not match: " + routeDefinition.getId()); }
         */
    }

//    private URI switchRoutes(String routeId, ServerWebExchange exchange){
//        HttpHeaders headers = exchange.getRequest().getHeaders();
//        JWT token = decodeBearerToken(headers);
//        String environment = (token != null) ? getClaimAsStringFromBearerToken(token, "env") : "";
//
//        if(Objects.nonNull(environment))
//        repository.findByRouteId(routeId)
//                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found")))
//                        .map(config -> {
////                            URI uri =
//                            if (environment.equalsIgnoreCase("uat")){
//                                uri =config.getTqUatUri();
//                            }
//                            if (environment.equalsIgnoreCase("test")){
//                                uri=config.getSandboxUri();
//                            }
//                            return config;
//                                    });
////        return URI.create();
//    }

    /**
     * Validate the given handler against the current request.
     * <p>
     * The default implementation is empty. Can be overridden in subclasses, for example
     * to enforce specific preconditions expressed in URL mappings.
     *
     * @param route    the Route object to validate
     * @param exchange current exchange
     * @throws Exception if validation failed
     */
    @SuppressWarnings("UnusedParameters")
    protected void validateRoute(Route route, ServerWebExchange exchange) {
    }

    protected String getSimpleName() {
        return "RoutePredicateHandlerMapping";
    }

    public enum ManagementPortType {

        /**
         * The management port has been disabled.
         */
        DISABLED,

        /**
         * The management port is the same as the server port.
         */
        SAME,

        /**
         * The management port and server port are different.
         */
        DIFFERENT;

    }

}
