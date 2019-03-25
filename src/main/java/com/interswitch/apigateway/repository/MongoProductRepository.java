package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoProductRepository extends ReactiveMongoRepository<Product,String> {
        Mono<Product> findByProductId(String productId);
}
