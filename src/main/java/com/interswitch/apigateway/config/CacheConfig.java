package com.interswitch.apigateway.config;

import com.interswitch.apigateway.model.ClientResources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheConfig {

    @Bean
    public ReactiveRedisOperations<String, ClientResources> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(ClientResources.class);
        RedisSerializationContext<String, ClientResources> serializationContext = RedisSerializationContext
                .<String, ClientResources>newSerializationContext(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .hashValue(jackson2JsonRedisSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
