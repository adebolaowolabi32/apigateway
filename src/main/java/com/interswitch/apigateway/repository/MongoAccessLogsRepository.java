package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.AccessLogs;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoAccessLogsRepository extends ReactiveMongoRepository<AccessLogs, String> {
}