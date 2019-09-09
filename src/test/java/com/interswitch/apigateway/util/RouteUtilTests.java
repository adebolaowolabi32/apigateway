package com.interswitch.apigateway.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class RouteUtilTests {
    @Autowired
    private RouteUtil routeUtil;

    public ServerWebExchange setup(String path) {
        ServerWebExchange exchange;
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080" + path)
                .build();
        exchange = MockServerWebExchange.from(request);
        return exchange;
    }

    @Test
    public void testIsGatewayEndpoint(){
        assertThat(routeUtil.isGatewayEndpoint(this.setup("/actuator/gateway/routes")).block()).isEqualTo(true);
        assertThat(routeUtil.isGatewayEndpoint(this.setup("/gateway/routes")).block()).isEqualTo(false);
    }

    @Test
    public void testIsInternalEndpoint(){
        assertThat(routeUtil.isInternalEndpoint(this.setup("/projects")).block()).isEqualTo(true);
        assertThat(routeUtil.isInternalEndpoint(this.setup("/actuator/gateway/routes")).block()).isEqualTo(false);
    }

    @Test
    public void testIsActuatorEndpoint(){
        assertThat(routeUtil.isActuatorEndpoint(this.setup("/actuator/metrics")).block()).isEqualTo(true);
        assertThat(routeUtil.isActuatorEndpoint(this.setup("/metrics")).block()).isEqualTo(false);
    }

    @Test
    public void testIsRouteBasedEndpoint(){
        assertThat(routeUtil.isRouteBasedEndpoint(this.setup("/passport/oauth/token")).block()).isEqualTo(true);
        assertThat(routeUtil.isRouteBasedEndpoint(this.setup("/users")).block()).isEqualTo(false);
    }
}
