package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.util.FilterUtil;
import com.nimbusds.jwt.JWT;
import org.springframework.boot.actuate.endpoint.web.reactive.ControllerEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class UserAccessFilter implements WebFilter, Ordered {

    private MongoUserRepository mongoUserRepository;

    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private ControllerEndpointHandlerMapping controllerEndpointHandlerMapping;

    private WebFluxEndpointHandlerMapping webFluxEndpointHandlerMapping;

    private FilterUtil filterUtil;

    public UserAccessFilter(MongoUserRepository mongoUserRepository, FilterUtil filterUtil, RequestMappingHandlerMapping requestMappingHandlerMapping, ControllerEndpointHandlerMapping controllerEndpointHandlerMapping, WebFluxEndpointHandlerMapping webFluxEndpointHandlerMapping){

        this.mongoUserRepository = mongoUserRepository;
        this.filterUtil = filterUtil;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.controllerEndpointHandlerMapping = controllerEndpointHandlerMapping;
        this.webFluxEndpointHandlerMapping = webFluxEndpointHandlerMapping;

    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return this.isInternalEndpoint(exchange).flatMap(isInternalEndpoint ->
                this.isGatewayEndpoint(exchange).flatMap(isGatewayEndpoint ->
                        this.isActuatorEndpoint((exchange)).flatMap(isActuatorEndpoint -> {
                           if( isInternalEndpoint || isGatewayEndpoint || isActuatorEndpoint){
                               JWT token = filterUtil.decodeBearerToken(exchange.getRequest().getHeaders());
                               String username = (token != null) ? filterUtil.getUsernameFromBearerToken(token) : "";
                               return mongoUserRepository.findByUsername(username)
                                       .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrator rights to access this resource")))
                                       .flatMap(user -> {
                                           if(user.getRole().equals(User.Role.ADMIN))
                                               return chain.filter(exchange);
                                           return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrator rights to access this resource"));
                                       });
                           }
                           return chain.filter(exchange);
               })));
    }

    public Mono<Boolean> isActuatorEndpoint(ServerWebExchange exchange){
        return webFluxEndpointHandlerMapping.getHandlerInternal(exchange).hasElement();
    }

    public Mono<Boolean> isInternalEndpoint(ServerWebExchange exchange){
        return requestMappingHandlerMapping.getHandlerInternal(exchange).hasElement();
    }

    public Mono<Boolean> isGatewayEndpoint(ServerWebExchange exchange){
        return controllerEndpointHandlerMapping.getHandlerInternal(exchange).hasElement();
    }

    @Override
    public int getOrder() {
        return -33456778;
    }
}