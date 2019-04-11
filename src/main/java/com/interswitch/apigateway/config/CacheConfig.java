package com.interswitch.apigateway.config;

import com.interswitch.apigateway.refresh.ClientCacheRefresh;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public ClientCacheRefresh clientCacheRefresh(ClientMongoRepository mongo, ClientCacheRepository cache){
        return new ClientCacheRefresh(mongo, cache);
    }

    @Bean
    public ClientCacheRepository clientCacheRepository(){
        return new ClientCacheRepository();
    }
}
