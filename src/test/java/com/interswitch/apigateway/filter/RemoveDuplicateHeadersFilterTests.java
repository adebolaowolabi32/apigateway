package com.interswitch.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest
@ActiveProfiles("dev")
public class RemoveDuplicateHeadersFilterTests {

    private RemoveDuplicateHeadersFilter filter;
    private GatewayFilterChain filterChain;
    private ArgumentCaptor<ServerWebExchange> captor;

    @BeforeEach
    public void setup() {
        filter = new RemoveDuplicateHeadersFilter();
        filterChain = mock(GatewayFilterChain.class);
        captor = ArgumentCaptor.forClass(ServerWebExchange.class);

    }

    @Test
    public void getRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().getHeaders().set("Host", "hello, hello, hello");
        exchange.getResponse().getHeaders().set("X-Key", "hi, hi, goodbye, goodbye, hi, goodbye");


        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain).block();

        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().get("Host")).containsOnlyOnce("hello");
        assertThat(webExchange.getResponse().getHeaders().get("X-Key")).containsOnlyOnce("hi,goodbye");


    }
}
