package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.AccessLogs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MongoAccessLogsRepository extends ReactiveMongoRepository<AccessLogs, String> {
    @Query(value = "{ $or: [ { 'username' : {$regex:?0,$options:'i'} }, { 'client' : {$regex:?0,$options:'i'} }, { 'entity' : {$regex:?0,$options:'i'} }, { 'action' : {$regex:?0,$options:'i'} }, { 'entityId' : {$regex:?0,$options:'i'} }, { 'api' : {$regex:?0,$options:'i'} }, { 'status' : {$regex:?0,$options:'i'} } ] }")
    Flux<AccessLogs> query(String query, Pageable page);

    @Query(value = "{ $or: [ { 'username' : {$regex:?0,$options:'i'} }, { 'client' : {$regex:?0,$options:'i'} }, { 'entity' : {$regex:?0,$options:'i'} }, { 'action' : {$regex:?0,$options:'i'} }, { 'entityId' : {$regex:?0,$options:'i'} }, { 'api' : {$regex:?0,$options:'i'} }, { 'status' : {$regex:?0,$options:'i'} } ] }", count = true)
    Mono<Long> count(String query);

    @Query("{ id: { $exists: true }}")
    Flux<AccessLogs> retrieveAllPaged(final Pageable page);

    @Query(value = "{ id: { $exists: true }}", count = true)
    Mono<Long> countAll();

   /* @Query(value = "{ 'timestamp' : { '$gt':{ '$date':'new ISODate(?0)'}, '$lt':{ '$date': 'new ISODate(?1)'}}}")
    Flux<AccessLogs> query(String from, String to, Pageable page);*/
}