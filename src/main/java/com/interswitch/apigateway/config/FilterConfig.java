package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.AccessControlFilter;
import com.interswitch.apigateway.filter.RouteIdFilter;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.util.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.interswitch.apigateway.filter.CorsFilter;

@Configuration
public class FilterConfig {
    @Bean
    public CorsFilter corsFilter(ClientCacheRepository cache, Client clientUtil){
        return new CorsFilter(cache, clientUtil);
    }
    @Bean
    public AccessControlFilter accessControlFilter(ClientCacheRepository cache, Client clientUtil){
        return new AccessControlFilter(cache, clientUtil);
    }
    @Bean
    public RouteIdFilter routeIdFilter(){
        return  new RouteIdFilter();
    }
}