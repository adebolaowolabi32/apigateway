package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.CorsFilter;
import com.interswitch.apigateway.filter.RouteIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public CorsFilter enableCorsFilter(){
        return new CorsFilter();
    }
    @Bean
    public RouteIdFilter routeIdFilter(){
        return new RouteIdFilter();
    }
}
