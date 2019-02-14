package com.interswitch.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest
public class EnableCorsFilterTests {

    private static final List<String> ALLOWED_HEADERS = Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
    private static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS);
    private static final long MAX_AGE = 3600;
    private static final Boolean ALLOW_CREDENTIALS = true;
    private static final String ALLOWED_ORIGIN = "Access-Control-Allow-Origin";

    private EnableCorsFilter filter;
    private WebFilterChain filterChain;
    private ArgumentCaptor<ServerWebExchange> captor;

    @BeforeEach
    public void setup() {
        filter = new EnableCorsFilter();
        filterChain = mock(WebFilterChain.class);
        captor = ArgumentCaptor.forClass(ServerWebExchange.class);

    }

    @Test
    public void getRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain);

        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().containsKey(ALLOWED_ORIGIN));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowHeaders()).containsAll(ALLOWED_HEADERS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowMethods().containsAll(ALLOWED_METHODS));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowCredentials()).isEqualTo(ALLOW_CREDENTIALS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlMaxAge()).isEqualTo(MAX_AGE);

    }

    @Test
    public void postRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain);

        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().containsKey(ALLOWED_ORIGIN));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowHeaders()).containsAll(ALLOWED_HEADERS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowMethods().containsAll(ALLOWED_METHODS));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowCredentials()).isEqualTo(ALLOW_CREDENTIALS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlMaxAge()).isEqualTo(MAX_AGE);

    }

    @Test
    public void optionsRequestFromAllowedOriginShouldReturnHttpStatusOkAndAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain);

        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().containsKey(ALLOWED_ORIGIN));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowHeaders()).containsAll(ALLOWED_HEADERS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowMethods().containsAll(ALLOWED_METHODS));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowCredentials()).isEqualTo(ALLOW_CREDENTIALS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlMaxAge()).isEqualTo(MAX_AGE);
        assertThat(webExchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void putRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain);

        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().containsKey(ALLOWED_ORIGIN));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowHeaders()).containsAll(ALLOWED_HEADERS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowMethods().containsAll(ALLOWED_METHODS));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowCredentials()).isEqualTo(ALLOW_CREDENTIALS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlMaxAge()).isEqualTo(MAX_AGE);

    }

    @Test
    public void deleteRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain);

        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().containsKey(ALLOWED_ORIGIN));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowHeaders()).containsAll(ALLOWED_HEADERS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowMethods().containsAll(ALLOWED_METHODS));
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowCredentials()).isEqualTo(ALLOW_CREDENTIALS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlMaxAge()).isEqualTo(MAX_AGE);

    }
}
