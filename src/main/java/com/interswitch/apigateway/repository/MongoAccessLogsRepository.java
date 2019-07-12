package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.AccessLogs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MongoAccessLogsRepository extends ReactiveMongoRepository<AccessLogs, String> {
    @Query(value = "{ $or: [ { 'username' : {$regex:?0,$options:'i'} }, { 'client' : {$regex:?0,$options:'i'} }," +
            " { 'api' : {$regex:?0,$options:'i'} }, { 'entity' : {$regex:?0,$options:'i'} }, { 'action' : {$regex:?0,$options:'i'} }," +
            "{ 'state' : {$regex:?0,$options:'i'} } ] }")
    Flux<AccessLogs> query(String query, Pageable page);

    @Query("{ id: { $exists: true }}")
    Flux<AccessLogs> retrieveAllPaged(final Pageable page);
}
