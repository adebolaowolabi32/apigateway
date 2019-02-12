package com.interswitch.apigateway.config;
import com.interswitch.apigateway.filter.EnableCorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public EnableCorsFilter enableCorsFilter(){
        return new EnableCorsFilter();
    }
}
