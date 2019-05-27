package com.interswitch.apigateway;

import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
public class TestConfig {
    @Bean(name = "mockConversionService")
    public ConversionService conversionService(){
        ConversionService conversionService = new WebConversionService("yyyy-MM-dd HH:mm:ss");
        return conversionService;
    }
}
