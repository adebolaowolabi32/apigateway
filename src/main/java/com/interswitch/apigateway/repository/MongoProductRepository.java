package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MongoProductRepository extends ReactiveMongoRepository<Product,String> {
    Mono<Boolean> existsByName(String name);
}
