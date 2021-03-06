package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.interswitch.apigateway.util.FilterUtil.isInterswitchEmail;

@RestController
@RequestMapping("/users")
public class UserController {

    private MongoUserRepository mongoUserRepository;

    public UserController(MongoUserRepository mongoUserRepository){
        this.mongoUserRepository = mongoUserRepository;
    }

    @GetMapping(produces = "application/json")
    private Flux<User> getAll() {
        return mongoUserRepository.findAll();
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<User> register(@Validated @RequestBody User user) {
        String username = user.getUsername().trim().toLowerCase();
        if (isInterswitchEmail(username)) {
            user.setUsername(username);
            user.setRole(User.Role.USER);
            return mongoUserRepository.save(user);
        }
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Username must be a valid Interswitch domain email address"));
    }

    @GetMapping(value = "/{username}", produces = "application/json")
    private Mono<User> findByUsername(@Validated @PathVariable String username) {
        return mongoUserRepository.findByUsername(username.toLowerCase())
                .switchIfEmpty(Mono.error(new NotFoundException("User does not exist")));
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<User> updateRole(@Validated @RequestBody User user) {
        return mongoUserRepository.findByUsername(user.getUsername().toLowerCase())
                .switchIfEmpty(Mono.error(new NotFoundException("User does not exist")))
                .flatMap(existing -> {
                    existing.setRole(user.getRole());
                    return mongoUserRepository.save(existing);
                });
    }

    @DeleteMapping("/{username}")
    private Mono<ResponseEntity<Void>> delete(@Validated @PathVariable String username) {
            return mongoUserRepository.findByUsername(username.toLowerCase())
                    .flatMap(user -> mongoUserRepository.deleteById(user.getId())
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                    .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}