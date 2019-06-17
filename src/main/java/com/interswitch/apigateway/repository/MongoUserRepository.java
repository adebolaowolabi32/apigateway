package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoUserRepository extends ReactiveMongoRepository<User, String> {
}
