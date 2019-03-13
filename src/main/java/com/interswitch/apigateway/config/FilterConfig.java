package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.AccessControlFilter;
import com.interswitch.apigateway.filter.RouteIdFilter;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.interswitch.apigateway.filter.CorsFilter;

@Configuration
public class FilterConfig {
    @Bean
    public CorsFilter corsFilter(){
        return new CorsFilter();
    }
    @Bean
    public AccessControlFilter accessControlFilter(ClientCacheRepository cacheRepository){
        return new AccessControlFilter(cacheRepository);
    }
    @Bean
    public RouteIdFilter routeIdFilter(){
        return  new RouteIdFilter();
    }
}