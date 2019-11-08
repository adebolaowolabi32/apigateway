package com.interswitch.apigateway.controller;


import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/audit")
public class AccessLogsController {
    private MongoAccessLogsRepository mongoAccessLogsRepository;

    public AccessLogsController(MongoAccessLogsRepository mongoAccessLogsRepository) {
        this.mongoAccessLogsRepository = mongoAccessLogsRepository;
    }

    @GetMapping(produces = "application/json")
    @Validated
    private Mono getAll(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Pageable page = PageRequest.of(pageNum, pageSize, new Sort(Sort.Direction.ASC, "timestamp"));
        Map<String, Object> record = new HashMap<>();
        List<AccessLogs> data = new ArrayList<>();

        return mongoAccessLogsRepository.countAll().flatMap(total -> {
            record.put("count", total);
            return mongoAccessLogsRepository.retrieveAllPaged(page).flatMap(accessLogs -> {
                data.add(accessLogs);
                return Mono.empty();
            }).then(Mono.defer(() -> {
                record.put("data", data);
                return Mono.just(record);
            }));
        });
    }

    @GetMapping(value = "/search", produces = "application/json")
    @Validated
    private Mono getSearchPaged(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                @RequestParam(value = "searchValue") String searchValue) {

        Map<String, Object> record = new HashMap<>();
        List<AccessLogs> data = new ArrayList<>();
        Pageable page = PageRequest.of(pageNum, pageSize, new Sort(Sort.Direction.ASC, "timestamp"));


        return mongoAccessLogsRepository.count(searchValue).flatMap(total -> {
            record.put("count", total);
            return mongoAccessLogsRepository.query(searchValue, page).flatMap(accessLogs -> {
                data.add(accessLogs);
                return Mono.empty();
            }).then(Mono.defer(() -> {
                record.put("data", data);
                return Mono.just(record);
            }));
        });
    }

/*    @GetMapping(value = "/searchTime", produces = "application/json")
    @Validated
    private Flux<AccessLogs> getSearch(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                       @RequestParam(value = "from", required = false) String from,
                                       @RequestParam(value = "to", required = false) String to) {

        Pageable page = PageRequest.of(pageNum, pageSize);
        String fromm = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss").format(LocalDateTime.parse(from).toInstant(ZoneOffset.UTC));
        String too = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss").format(LocalDateTime.parse(from).toInstant(ZoneOffset.UTC));
       return mongoAccessLogsRepository.query(fromm, too, page);

    }*/
}
