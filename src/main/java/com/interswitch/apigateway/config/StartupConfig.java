package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ClientResourcesRepository;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

public class StartupConfig {
    @Value("${passport.baseurl}")
    String baseUrl;

    @Bean
    public CommandLineRunner commandLineRunnerCache(MongoClientResourcesRepository mongo, ClientResourcesRepository cache){

        return commandLineRunnerCache -> {
            mongo.findAll().flatMap(clientResources -> cache.save(clientResources)).subscribe();
        };
    }
}
