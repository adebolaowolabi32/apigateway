package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Projects;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MongoProjectsRepository extends ReactiveMongoRepository<Projects, String> {
    Mono<Projects> findByAppId(String appId);
    
}
