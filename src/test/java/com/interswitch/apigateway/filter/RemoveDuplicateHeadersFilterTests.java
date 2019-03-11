package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {ClientMongoRepository.class, RemoveDuplicateHeadersFilter.class})
public class RemoveDuplicateHeadersFilterTests {

    @Autowired
    private RemoveDuplicateHeadersFilter filter;
    private GatewayFilterChain filterChain;
    private ArgumentCaptor<ServerWebExchange> captor;

    @MockBean
    private ClientMongoRepository clientMongoRepository;


    @BeforeEach
    public void setup() {
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
