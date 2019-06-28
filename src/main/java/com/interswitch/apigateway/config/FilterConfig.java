package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.*;
import com.interswitch.apigateway.repository.MongoClientRepository;
import com.interswitch.apigateway.util.FilterUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public RouteIdFilter routeIdFilter(){
        return  new RouteIdFilter();
    }
    @Bean
    public AudienceFilter audienceFilter(FilterUtil filterUtil) {return new AudienceFilter(filterUtil); }
    @Bean
    public LoggingFilter loggingFilter(){return new LoggingFilter();}
}


