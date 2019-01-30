package com.interswitch.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

import static net.logstash.logback.argument.StructuredArguments.value;

@RestController
@Slf4j
public class HelloController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();


    @GetMapping("/greeting")
    public Mono<Greeting> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        var greeting = new Greeting(counter.incrementAndGet(), String.format(template, name));
        log.info("Message received", value("greeting", greeting));
        return Mono.just(greeting);
    }
}