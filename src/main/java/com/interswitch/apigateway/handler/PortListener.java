package com.interswitch.apigateway.handler;

import com.interswitch.apigateway.service.PassportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.context.ReactiveWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PortListener implements ApplicationListener<ReactiveWebServerInitializedEvent> {

    @Autowired
    PassportService passportService;

    @Override
    public void onApplicationEvent(final ReactiveWebServerInitializedEvent event) {
        Integer port = event.getWebServer().getPort();
        passportService.buildWebClient(port);
    }
}