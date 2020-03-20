package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.*;
import com.interswitch.apigateway.handler.RouteHandlerMapping;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.route.MongoRouteDefinitionRepository;
import com.interswitch.apigateway.util.RouteUtil;
import com.interswitch.apigateway.util.SecurityUtil;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.endpoint.web.reactive.ControllerEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class FilterConfig {
    @Bean
    public CorsFilter corsFilter(){
        return new CorsFilter();
    }

    @Bean
    public RouteAccessControlFilter routeAccessControlFilter(SecurityUtil securityUtil) {
        return new RouteAccessControlFilter(securityUtil);
    }

    @Bean
    public AccessControlFilter accessControlFilter(MongoUserRepository mongoUserRepository, RouteUtil routeUtil) {
        return new AccessControlFilter(mongoUserRepository, routeUtil);
    }

    @Bean
    public SecurityFilter securityFilter(RouteUtil routeUtil) {
        return new SecurityFilter(routeUtil);
    }

    @Bean
    public LoggingFilter loggingFilter(MeterRegistry meterRegistry) {
        return new LoggingFilter(meterRegistry);
    }

    @Bean
    public RouteUtil routeUtil(RequestMappingHandlerMapping requestMappingHandlerMapping, ControllerEndpointHandlerMapping controllerEndpointHandlerMapping, WebFluxEndpointHandlerMapping webFluxEndpointHandlerMapping, RouteHandlerMapping routeHandlerMapping, SecurityUtil securityUtil) {
        return new RouteUtil(requestMappingHandlerMapping, controllerEndpointHandlerMapping, webFluxEndpointHandlerMapping, routeHandlerMapping, securityUtil);
    }

    @Bean
    public SecurityUtil securityUtil(MongoRouteDefinitionRepository routeDefinitionRepository) {
        return new SecurityUtil(routeDefinitionRepository);
    }

    @Bean
    public AccessLogsFilter accessLogsFilter(MongoAccessLogsRepository mongoAccessLogsRepository, RouteUtil routeUtil) {
        return new AccessLogsFilter(mongoAccessLogsRepository, routeUtil);
    }

    @Bean
    public DownstreamTimerFilter downstreamTimerFilter(MeterRegistry meterRegistry) {
        return new DownstreamTimerFilter(meterRegistry);
    }

    @Bean
    public ResponseInterceptor responseInterceptor() {
        return new ResponseInterceptor();
    }
}

