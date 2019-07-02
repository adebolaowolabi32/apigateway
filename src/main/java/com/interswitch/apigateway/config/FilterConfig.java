package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.*;
import com.interswitch.apigateway.repository.MongoClientRepository;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.util.FilterUtil;
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
    public AccessControlFilter accessControlFilter(MongoClientRepository mongo, FilterUtil filterUtil){
        return new AccessControlFilter(mongo,filterUtil);
    }
    @Bean
    public UserAccessFilter userAccessFilter(MongoUserRepository mongoUserRepository, FilterUtil filterUtil, RequestMappingHandlerMapping requestMappingHandlerMapping, ControllerEndpointHandlerMapping controllerEndpointHandlerMapping, WebFluxEndpointHandlerMapping webFluxEndpointHandlerMapping){
        return new UserAccessFilter(mongoUserRepository, filterUtil, requestMappingHandlerMapping, controllerEndpointHandlerMapping, webFluxEndpointHandlerMapping);
    }
    @Bean
    public RouteIdFilter routeIdFilter(){
        return  new RouteIdFilter();
    }
    @Bean
    public AudienceFilter audienceFilter(FilterUtil filterUtil) {return new AudienceFilter(filterUtil); }
    @Bean
    public LoggingFilter loggingFilter(){return new LoggingFilter();}

    @Bean
    public FilterUtil filterUtil(){
        return new FilterUtil();
    }
}


