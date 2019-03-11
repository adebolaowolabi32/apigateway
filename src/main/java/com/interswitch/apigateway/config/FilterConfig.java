package com.interswitch.apigateway.config;
import com.interswitch.apigateway.filter.AccessControlFilter;
import com.interswitch.apigateway.filter.EnableCorsFilter;
import com.interswitch.apigateway.filter.RemoveDuplicateHeadersFilter;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public EnableCorsFilter enableCorsFilter(){
        return new EnableCorsFilter();
    }
    @Bean
    public RemoveDuplicateHeadersFilter removeDuplicateHeadersFilter(){
        return new RemoveDuplicateHeadersFilter();
    }
    @Bean
    public AccessControlFilter accessControlFilter(ClientCacheRepository repository){return new AccessControlFilter(repository);}
}
