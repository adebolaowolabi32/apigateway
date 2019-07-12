package com.interswitch.apigateway.controller;


import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
public class AccessLogsController {
    MongoAccessLogsRepository mongoAccessLogsRepository;

    public AccessLogsController(MongoAccessLogsRepository mongoAccessLogsRepository) {
        this.mongoAccessLogsRepository = mongoAccessLogsRepository;
    }

    @GetMapping(value= "/{page}", produces = "application/json")
    private Page<AccessLogs> getAll(@PathVariable int page) {
        return mongoAccessLogsRepository.findAll();
    }
}
