package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.AccessLogs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public interface MongoAccessLogsRepository extends ReactiveMongoRepository<AccessLogs, String> {
    @Query("{id: { $exists: true}}")
    Flux<AccessLogs> findAll(Pageable pageable);
}
