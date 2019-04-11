package com.interswitch.apigateway.refresh;

import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

public class AutoBusRefresh {
    private ApplicationEventPublisher context;
    private String appId;

    public AutoBusRefresh(ApplicationEventPublisher context, BusProperties bus){
        this.context = context;
        this.appId = bus.getId();
    }

    public void publishRefreshEvent(){
        this.context.publishEvent(new RefreshRemoteApplicationEvent(this,appId, null));
    }
}
