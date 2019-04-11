package com.interswitch.apigateway.refresh;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;

public class ClientCacheRefresh {

    private ClientMongoRepository mongo;
    private ClientCacheRepository cache;


    public ClientCacheRefresh(ClientMongoRepository mongo, ClientCacheRepository cache){
        this.mongo = mongo;
        this.cache = cache;
    }

    @EventListener(RefreshRemoteApplicationEvent.class)
    public void refresh(RefreshRemoteApplicationEvent event){
        cache.clear();
        mongo.findAll().flatMap(client -> cache.save(Mono.just(client))).subscribe();
    }

}
