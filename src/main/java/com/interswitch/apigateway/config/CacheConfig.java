package com.interswitch.apigateway.config;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ClientResourcesRepository;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisOperations;

public class CacheConfig {

    @Bean
    public ClientResourcesRepository clientResourcesRepository(ReactiveRedisOperations<String, ClientResources> template){
        return new ClientResourcesRepository(template);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ClientResourcesRepository repository, MongoClientResourcesRepository mongoRepository){

        return commandLineRunner -> {
            mongoRepository.findAll().map(clientResources -> {
                repository.save(clientResources).log().subscribe();
                return clientResources;
            }).subscribe();
        };
    }
}
