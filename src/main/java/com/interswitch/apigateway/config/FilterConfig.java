package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.*;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import com.interswitch.apigateway.repository.MongoClientRepository;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.util.FilterUtil;
import com.interswitch.apigateway.util.RouteUtil;
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
    public UserAccessFilter userAccessFilter(MongoUserRepository mongoUserRepository, FilterUtil filterUtil, RouteUtil routeUtil){
        return new UserAccessFilter(mongoUserRepository, filterUtil, routeUtil);
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

    @Bean
    public RouteUtil routeUtil(RequestMappingHandlerMapping requestMappingHandlerMapping, ControllerEndpointHandlerMapping controllerEndpointHandlerMapping, WebFluxEndpointHandlerMapping webFluxEndpointHandlerMapping) {
        return new RouteUtil(requestMappingHandlerMapping, controllerEndpointHandlerMapping, webFluxEndpointHandlerMapping);
    }

    @Bean
    public AccessLogsFilter accessLogsFilter(MongoAccessLogsRepository mongoAccessLogsRepository, FilterUtil filterUtil, RouteUtil routeUtil){
        return new AccessLogsFilter(mongoAccessLogsRepository, filterUtil, routeUtil);
    }

}


