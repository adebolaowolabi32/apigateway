package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.model.Resource;
import com.interswitch.apigateway.repository.MongoProductRepository;
import com.interswitch.apigateway.repository.MongoResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private MongoProductRepository mongoProductRepository;

    private MongoResourceRepository mongoResourceRepository;

    public ProductController(MongoProductRepository mongoProductRepository,MongoResourceRepository mongoResourceRepository) {
        this.mongoProductRepository = mongoProductRepository;
        this.mongoResourceRepository = mongoResourceRepository;
    }

    @GetMapping(produces = "application/json")
    private Flux<Product> getAll() {
        return mongoProductRepository.findAll();
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Product> save(@Validated @RequestBody Product product) {
        product.setResources(new ArrayList<>());
        product.setClients(new ArrayList<>());
        return mongoProductRepository.save(product).onErrorMap(throwable -> {
            return new ResponseStatusException(HttpStatus.CONFLICT,"Product already exists");
        });
    }

    @GetMapping(value = "/{productId}", produces = "application/json")
    private Mono<ResponseEntity<Product>> findById(@Validated @PathVariable String productId) {
        return mongoProductRepository.findById(productId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<Product> update(@Validated @RequestBody Product product) {
        return mongoProductRepository.findById(product.getId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Product does not exist")))
                .flatMap(existing -> {
                    product.setId(existing.getId());
                    product.setClients(existing.getClients());
                    product.setResources(existing.getResources());
                    return mongoProductRepository.save(product).onErrorMap(throwable -> {
                        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Product was not modified");
                    });
                });
    }

    @DeleteMapping("/{productId}")
    private Mono<ResponseEntity<Void>> delete(@PathVariable String productId) {
        try {
            return mongoProductRepository.findById(productId)
                    .flatMap(product -> {
                        product.getResources().forEach(resource -> mongoResourceRepository.deleteById(resource.getId()));
                        return mongoProductRepository.deleteById(productId).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
                    });
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

    }

    @GetMapping(value = "/{productId}/resources", produces = "application/json")
    private Mono<List<Resource>> getResources(@PathVariable String productId) {
        return mongoProductRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Product does not exist")))
                .map(product -> product.getResources());
    }

    @PostMapping(value = "/{productId}/resources", produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Product> saveResource(@PathVariable String productId, @Validated @RequestBody Resource resource) {
        return mongoProductRepository.findById(productId)
                .flatMap(product -> {
                    resource.setProduct(product);
                    return mongoResourceRepository.save(resource).flatMap(r -> {
                        if(!product.getResources().contains(r)){
                            product.addResource(r);
                            return mongoProductRepository.save(product);
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,"Resource already assigned to Product"));});
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Product does not exist")));
    }

    @GetMapping(value = "/{productId}/resources/{resourceId}", produces = "application/json")
    private Mono<ResponseEntity<Resource>> findResourceById(@PathVariable String resourceId) {
        return mongoResourceRepository.findById(resourceId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping(value = "/{productId}/resources", produces = "application/json", consumes = "application/json")
    private Mono<Product> updateResource(@PathVariable String productId, @Validated @RequestBody Resource resource) {
        return mongoProductRepository.findById(productId)
                .flatMap(product -> mongoResourceRepository.findById(resource.getId()).flatMap(existing -> {
                    if(product.getResources().contains(existing)) {
                        resource.setId(existing.getId());
                        resource.setProduct(product);
                        return mongoResourceRepository.save(resource).flatMap(r -> {
                            product.removeResource(existing);
                            product.addResource(r);
                            return mongoProductRepository.save(product).onErrorMap(throwable -> {
                                return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Resource was not modified");
                            });
                        });
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Resource not found for Product"));
                }).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Resource does not exist"))))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Product does not exist")));
    }

    @DeleteMapping("/{productId}/resources/{resourceId}")
    private Mono<Product> deleteResource(@PathVariable String productId, @PathVariable String resourceId) {
        return mongoProductRepository.findById(productId).flatMap(product ->
                        mongoResourceRepository.findById(resourceId).flatMap(resource -> {
                             if(product.getResources().contains(resource)) {
                                    product.removeResource(resource);
                             }
                            return mongoResourceRepository.deleteById(resourceId).then(mongoProductRepository.save(product));
                        })
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Resource does not exist"))))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Product does not exist")));
    }
}
