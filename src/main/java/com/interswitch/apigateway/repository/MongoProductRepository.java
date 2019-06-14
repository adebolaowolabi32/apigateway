package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoProductRepository extends ReactiveMongoRepository<Product,String> {
}
