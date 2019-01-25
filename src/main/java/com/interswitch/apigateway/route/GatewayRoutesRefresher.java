package com.interswitch.apigateway.route;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public class GatewayRoutesRefresher implements ApplicationEventPublisherAware {

    ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    public void refreshRoutes() {
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }
}