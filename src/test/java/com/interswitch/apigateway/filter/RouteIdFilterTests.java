package com.interswitch.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.net.URISyntaxException;;

import static org.mockito.Mockito.when;


@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {RouteIdFilter.class})
public class RouteIdFilterTests {
    @Autowired
    private RouteIdFilter filter;

    private ArgumentCaptor<ServerWebExchange> captor;

    @MockBean
    private WebFilterChain filterChain;
    private String id = "testapi";

    @BeforeEach
    public void setup() throws URISyntaxException {
        captor = ArgumentCaptor.forClass(ServerWebExchange.class);
    }


    @Test
    public void test() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/actuator/gateway/routes/"+id)
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain)).expectComplete().verify();
    }

}
