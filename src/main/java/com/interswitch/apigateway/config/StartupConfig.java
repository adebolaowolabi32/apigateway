package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupConfig {
    @Bean
    public CommandLineRunner commandLineRunnerCache(ClientMongoRepository mongo, ClientCacheRepository cache){
        return commandLineRunnerCache -> {
            mongo.findAll().flatMap(clientResources -> cache.save(clientResources)).subscribe();
        };
    }
}
