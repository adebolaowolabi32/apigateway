package com.interswitch.apigateway.config;

import com.interswitch.apigateway.refresh.AutoBusRefresh;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusRefreshConfig {
    @Bean
    public AutoBusRefresh autoBusRefresh(ApplicationContext context, BusProperties bus){
        return new AutoBusRefresh(context,bus);
    }
}
