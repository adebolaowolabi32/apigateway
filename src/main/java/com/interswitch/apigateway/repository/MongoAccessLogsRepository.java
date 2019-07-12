package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.AccessLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface MongoAccessLogsRepository extends ReactiveMongoRepository<AccessLogs, String> {
    Page<AccessLogs> findAll(Pageable pageable);
}
