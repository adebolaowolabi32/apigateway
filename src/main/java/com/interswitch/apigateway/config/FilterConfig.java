package com.interswitch.apigateway.config;

import com.interswitch.apigateway.filter.AccessControlFilter;
import com.interswitch.apigateway.filter.RouteIdFilter;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.util.ClientPermissionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.interswitch.apigateway.filter.CorsFilter;

@Configuration
public class FilterConfig {
    @Bean
    public CorsFilter corsFilter(ClientCacheRepository cache, ClientPermissionUtils util){
        return new CorsFilter(cache, util);
    }
    @Bean
    public AccessControlFilter accessControlFilter(ClientCacheRepository cache, ClientPermissionUtils util){
        return new AccessControlFilter(cache, util);
    }
    @Bean
    public RouteIdFilter routeIdFilter(){
        return  new RouteIdFilter();
    }
}