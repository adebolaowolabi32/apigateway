package com.interswitch.apigateway.controller;


import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/audit")
public class AccessLogsController {
    private MongoAccessLogsRepository mongoAccessLogsRepository;

    public AccessLogsController(MongoAccessLogsRepository mongoAccessLogsRepository) {
        this.mongoAccessLogsRepository = mongoAccessLogsRepository;
    }

    @GetMapping(produces = "application/json")
    @Validated
    private Flux<AccessLogs> getAll(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Pageable page = PageRequest.of(pageNum, pageSize);
        return mongoAccessLogsRepository.retrieveAllPaged(page);
    }

    @GetMapping(value = "/search", produces = "application/json")
    @Validated
    private Flux<AccessLogs> getSearchPaged(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                            @RequestParam(value = "searchValue") String searchValue,
                                            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Pageable page = PageRequest.of(pageNum, pageSize);
        return mongoAccessLogsRepository.query(searchValue, page);
    }
}
