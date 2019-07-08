package com.interswitch.apigateway.controller;


import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/audit")
public class AccessLogsController {
    MongoAccessLogsRepository mongoAccessLogsRepository;

    public AccessLogsController(MongoAccessLogsRepository mongoAccessLogsRepository) {
        this.mongoAccessLogsRepository = mongoAccessLogsRepository;
    }

    @GetMapping(produces = "application/json")
    private Flux<AccessLogs> getAll() {
        return mongoAccessLogsRepository.findAll();
    }
}
