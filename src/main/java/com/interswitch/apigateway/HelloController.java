package com.interswitch.apigateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

import static net.logstash.logback.argument.StructuredArguments.value;

@RestController
public class HelloController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Value("${spring.redis.sentinel.master}")
    private String greetingmessage;

    @GetMapping("/greeting")
    public Mono<Greeting> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        var greeting = new Greeting(counter.incrementAndGet(), greetingmessage);
        logger.info("Message", greeting);
        return Mono.just(greeting);
    }
}