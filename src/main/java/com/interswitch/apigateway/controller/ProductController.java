package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.model.Resource;
import com.interswitch.apigateway.repository.MongoProductRepository;
import com.interswitch.apigateway.repository.MongoResourceRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.Set;

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
        product.setName(product.getName().toLowerCase());
        product.setResources(new LinkedHashSet<>());
        product.setClients(new LinkedHashSet<>());
        return mongoProductRepository.save(product);
    }

    @GetMapping(value = "/{productId}", produces = "application/json")
    private Mono<Product> findById(@Validated @PathVariable String productId) {
        return mongoProductRepository.findById(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product does not exist")));
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    private Mono<Product> update(@Validated @RequestBody Product product) {
        return mongoProductRepository.findById(product.getId())
                .switchIfEmpty(Mono.error(new NotFoundException("Product does not exist")))
                .flatMap(existing -> {
                    product.setId(existing.getId());
                    product.setClients(existing.getClients());
                    product.setResources(existing.getResources());
                    product.setName(product.getName().toLowerCase());
                    return mongoProductRepository.save(product);
                });
    }

    @DeleteMapping("/{productId}")
    private Mono<ResponseEntity<Void>> delete(@Validated @PathVariable String productId) {
        return mongoProductRepository.findById(productId)
                .flatMap(product -> {
                    return mongoProductRepository.deleteById(productId)
                            .then(Mono.defer(() -> {
                                return Flux.fromIterable(product.getResources()).flatMap(r -> {
                                    return mongoResourceRepository.deleteById(r.getId());
                                }).then();
                            }))
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
                }).switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));

    }

    @GetMapping(value = "/{productId}/resources", produces = "application/json")
    private Mono<Set<Resource>> getResources(@Validated @PathVariable String productId) {
        return mongoProductRepository.findById(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product does not exist")))
                .map(product -> product.getResources());
    }

    @PostMapping(value = "/{productId}/resources", produces = "application/json", consumes = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Product> saveResource(@Validated @PathVariable String productId, @Validated @RequestBody Resource resource) {
        return mongoProductRepository.findById(productId)
                .flatMap(product -> {
                    resource.setName(resource.getName().toLowerCase());
                    resource.setProduct(product);
                    return mongoResourceRepository.save(resource).flatMap(r -> {
                        if(!product.getResources().contains(r)){
                            product.addResource(r);
                        }
                        return mongoProductRepository.save(product);
                    });
                }).switchIfEmpty(Mono.error(new NotFoundException("Product does not exist")));
    }

    @GetMapping(value = "/{productId}/resources/{resourceId}", produces = "application/json")
    private Mono<Resource> findResourceById(@Validated @PathVariable String productId, @Validated @PathVariable String resourceId) {
        return mongoProductRepository.findById(productId)
                .flatMap(product -> {
                    return mongoResourceRepository.findById(resourceId).flatMap(resource -> {
                        if (product.getResources().contains(resource))
                            return Mono.just(resource);
                        return Mono.error(new NotFoundException("Resource does not exist in Product"));
                    }).switchIfEmpty(Mono.error(new NotFoundException("Resource does not exist")));
                }).switchIfEmpty(Mono.error(new NotFoundException("Product does not exist")));

    }

    @PutMapping(value = "/{productId}/resources", produces = "application/json", consumes = "application/json")
    private Mono<Product> updateResource(@Validated @PathVariable String productId, @Validated @RequestBody Resource resource) {
        return mongoProductRepository.findById(productId)
                .flatMap(product -> {
                    return mongoResourceRepository.findById(resource.getId()).flatMap(existing -> {
                        if (product.getResources().contains(existing)) {
                            resource.setId(existing.getId());
                            resource.setName(resource.getName().toLowerCase());
                            resource.setProduct(product);
                            return mongoResourceRepository.save(resource).flatMap(r -> {
                                product.removeResource(existing);
                                product.addResource(r);
                                return mongoProductRepository.save(product);
                            });
                        }
                        return Mono.error(new NotFoundException("Resource not found for Product"));
                    }).switchIfEmpty(Mono.error(new NotFoundException("Resource does not exist")));
                }).switchIfEmpty(Mono.error(new NotFoundException("Product does not exist")));
    }

    @DeleteMapping("/{productId}/resources/{resourceId}")
    private Mono<ResponseEntity<Void>> deleteResource(@Validated @PathVariable String productId, @Validated @PathVariable String resourceId) {
        return mongoProductRepository.findById(productId).flatMap(product -> {
            return mongoResourceRepository.findById(resourceId).flatMap(resource -> {
                if (product.getResources().contains(resource)) {
                    product.removeResource(resource);
                    return mongoResourceRepository.deleteById(resourceId)
                            .then(mongoProductRepository.save(product))
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
                }
                return Mono.just(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));

            }).switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
        }).switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
