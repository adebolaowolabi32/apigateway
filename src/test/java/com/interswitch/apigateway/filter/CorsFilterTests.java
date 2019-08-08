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

import static com.interswitch.apigateway.filter.CorsFilter.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {CorsFilter.class})
public class CorsFilterTests {

    @Autowired
    private CorsFilter filter;
    private ArgumentCaptor<ServerWebExchange> captor;

    @MockBean
    private WebFilterChain filterChain;

    @BeforeEach
    public void setup() {
        captor = ArgumentCaptor.forClass(ServerWebExchange.class);
    }

    @Test
    public void getRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost")
                .build();

        assertThatResponseContainsHeaders(request);

    }

    @Test
    public void postRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost")
                .build();

        assertThatResponseContainsHeaders(request);

    }

    @Test
    public void optionsRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost")
                .build();

        ServerWebExchange webExchange = assertThatResponseContainsHeaders(request);

    }

    @Test
    public void putRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .put("http://localhost")
                .build();

        assertThatResponseContainsHeaders(request);

    }

    @Test
    public void deleteRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .delete("http://localhost")
                .build();

        assertThatResponseContainsHeaders(request);

    }

    private ServerWebExchange assertThatResponseContainsHeaders(MockServerHttpRequest request){
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());
        filter.filter(exchange, filterChain).block();

        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowOrigin()).isEqualTo(ALLOWED_ORIGIN);
        assertThat(webExchange.getResponse().getHeaders().getVary()).isEqualTo(VARY);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowHeaders()).containsAll(ALLOWED_HEADERS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowMethods()).containsAll(ALLOWED_METHODS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowCredentials()).isEqualTo(ALLOW_CREDENTIALS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlMaxAge()).isEqualTo(MAX_AGE);

        return webExchange;
    }
}
