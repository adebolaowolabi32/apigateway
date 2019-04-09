package com.interswitch.apigateway.config;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import reactor.core.publisher.Mono;

@Configuration
public class CacheConfig {

    @RefreshScope
    @DependsOn("clientMongoRepository")
    @Bean
    public ClientCacheRepository clientCacheRepository(ClientMongoRepository clientMongoRepository){
        return new ClientCacheRepository(clientMongoRepository);
           // clientMongoRepository.findAll().flatMap(client -> clientCacheRepository.save(Mono.just(client))).subscribe();
    }

/*    @Bean
    public CommandLineRunner loadClientCache(ClientMongoRepository mongoClientRepo, ClientCacheRepository cacheClientRepo){
        return loadClientCache -> {
            mongoClientRepo.findAll().flatMap(client -> cacheClientRepo.save(Mono.just(client))).subscribe();
        };
    }*/

}
