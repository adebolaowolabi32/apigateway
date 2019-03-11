package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

public class StartupConfig {
    @Value("${passport.baseurl}")
    String baseUrl;

    @Bean
    public CommandLineRunner commandLineRunnerCache(ClientMongoRepository mongo, ClientCacheRepository cache){

        return commandLineRunnerCache -> {
            mongo.findAll().flatMap(clientResources -> cache.save(clientResources)).subscribe();
        };
    }
}
