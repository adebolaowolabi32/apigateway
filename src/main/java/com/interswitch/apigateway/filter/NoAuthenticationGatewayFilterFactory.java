package com.interswitch.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Pattern;

@Component
public class NoAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<NoAuthenticationGatewayFilterFactory.Config> {

    public NoAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> chain.filter(exchange);
    }

    public static class Config {
        private HttpMethod method;

        @Pattern(regexp = "^/.+$", message = "Path must be a valid request path")
        private String path;

        public Config() {
        }

        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
