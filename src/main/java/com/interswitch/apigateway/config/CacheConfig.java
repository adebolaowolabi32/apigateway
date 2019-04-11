package com.interswitch.apigateway.config;

import com.interswitch.apigateway.refresh.AutoBusRefresh;
import com.interswitch.apigateway.refresh.ClientCacheRefresh;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class CacheConfig {

    @Bean
    public AutoBusRefresh autoBusRefresh(ApplicationContext context, BusProperties bus){
        return new AutoBusRefresh(context,bus);
    }

    @Bean
    public ClientCacheRefresh clientCacheRefresh(ClientMongoRepository mongo, ClientCacheRepository cache){
        return new ClientCacheRefresh(mongo, cache);
    }

    @Bean
    public ClientCacheRepository clientCacheRepository(){
        return new ClientCacheRepository();
    }

    @Bean
    public CommandLineRunner loadClients(ClientMongoRepository mongo, ClientCacheRepository cache){
        return loadClients -> {
            mongo.findAll().flatMap(client -> cache.save(Mono.just(client))).subscribe();
        };
    }

}
