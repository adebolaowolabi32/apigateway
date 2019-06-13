package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.AccessControlFilter;
import com.interswitch.apigateway.filter.CorsFilter;
import com.interswitch.apigateway.filter.LoggingFilter;
import com.interswitch.apigateway.filter.RouteIdFilter;
import com.interswitch.apigateway.repository.MongoClientRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public CorsFilter corsFilter(){
        return new CorsFilter();
    }
    @Bean
    public AccessControlFilter accessControlFilter(MongoClientRepository mongo){
        return new AccessControlFilter(mongo);
    }
    @Bean
    public RouteIdFilter routeIdFilter(){
        return  new RouteIdFilter();
    }

    @Bean
    public LoggingFilter loggingFilter(){return new LoggingFilter();}
}


