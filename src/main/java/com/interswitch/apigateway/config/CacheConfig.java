package com.interswitch.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ClientResourcesRepository;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Mono;

@Configuration
public class CacheConfig {

    @Bean
    public ReactiveRedisTemplate<String, ClientResources> reactiveJsonSessionRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(ClientResources.class);
        RedisSerializationContext<String, ClientResources> serializationContext = RedisSerializationContext
                .<String, ClientResources>newSerializationContext(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .hashValue(jackson2JsonRedisSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Bean
    public ClientResourcesRepository clientResourcesRepository(ReactiveRedisTemplate<String, ClientResources> template){
        return new ClientResourcesRepository(template);
    }

    @Bean
    public CommandLineRunner commandLineRunnerCache(ClientResourcesRepository cache, MongoClientResourcesRepository clientResourcesRepository){

        return commandLineRunnerCache -> {
            clientResourcesRepository.findAll().flatMap(clientResources -> cache.save(clientResources)).subscribe();
        };
    }
}
