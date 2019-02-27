package com.interswitch.apigateway.controller;

import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ReactiveMongoClientResources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/resources")
public class ClientResourcesController {

    private ReactiveMongoClientResources clientResources;

    public ClientResourcesController(ReactiveMongoClientResources clientResources) {
        this.clientResources = clientResources;
    }

    @GetMapping(produces = "application/json")
    private Flux<ClientResources> getAllClientResources(){
        try{
            return clientResources.findAll();
        }
        catch (Exception e){
            return null;
        }
    }
}
