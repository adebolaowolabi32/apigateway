package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MongoProjectRepository extends ReactiveMongoRepository<Project, String> {
    Flux<Project> findByOwner(String owner);

    Mono<Boolean> existsByName(String name);
}
