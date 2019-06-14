package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.Resource;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoResourceRepository extends ReactiveMongoRepository<Resource, String> {
}
