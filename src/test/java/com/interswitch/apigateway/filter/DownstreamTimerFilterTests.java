package com.interswitch.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.interswitch.apigateway.filter.DownstreamTimerFilter.DOWNSTREAM_ROUTE_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class DownstreamTimerFilterTests {

    @Autowired
    private DownstreamTimerFilter filter;

    private ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

    @MockBean
    private GatewayFilterChain filterChain;

    @Test
    public void timeDownstreamRequest() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/anyPath")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());
        filter.filter(exchange, filterChain).block();

        ServerWebExchange webExchange = captor.getValue();
        assertThat((Long) webExchange.getAttribute(DOWNSTREAM_ROUTE_DURATION)).isGreaterThan(0);

    }
}
