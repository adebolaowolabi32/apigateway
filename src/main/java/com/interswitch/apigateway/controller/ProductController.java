package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.Product;
import com.interswitch.apigateway.repository.MongoProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {

    private MongoProductRepository mongoProductRepository;

    public ProductController(MongoProductRepository mongoProductRepository) {
        this.mongoProductRepository = mongoProductRepository;
    }

    @GetMapping(produces = "application/json")
    private Flux<Product> getAllProduct() {
        return mongoProductRepository.findAll();
    }

    @PostMapping(value = "/save", produces = "application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    private Mono<Product> saveProduct(@Validated @RequestBody Product product) {
        return mongoProductRepository.save(product);
    }

    @GetMapping(value = "/{productId}", produces = "application/json")
    private Mono<ResponseEntity<Product>> findProduct(@Validated @PathVariable String productId) {
        return mongoProductRepository.findByProductId(productId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping(value = "/update", produces = "application/json")
    private Mono<Product> updateProduct(@Validated @RequestBody Product product) {
        return mongoProductRepository.findByProductId(product.getProductId())
                .flatMap(existing -> {
                    existing.setProductName(product.getProductName());
                    existing.setResourceIds(product.getResourceIds());
                    existing.setProductDescription(product.getProductDescription());
                    return mongoProductRepository.save(existing);
                });
    }

    @DeleteMapping("/delete/{id}")
    private Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        try {
            return mongoProductRepository.deleteById(id)
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

    }
}
