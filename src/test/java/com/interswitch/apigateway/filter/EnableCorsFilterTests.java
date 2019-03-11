package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {ClientMongoRepository.class, ClientCacheRepository.class, EnableCorsFilter.class})
public class EnableCorsFilterTests {

    private static final List<String> ALLOWED_HEADERS = Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization");
    private static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.OPTIONS);
    private static final long MAX_AGE = 3600;
    private static final Boolean ALLOW_CREDENTIALS = true;
    private static final String ALLOWED_ORIGIN = "http://localhost:3000";

    @Autowired
    private EnableCorsFilter filter;
    private ArgumentCaptor<ServerWebExchange> captor;
    private HttpHeaders headers;

    @MockBean
    private WebFilterChain filterChain;

    @MockBean
    private ClientMongoRepository clientMongoRepository;

    @MockBean
    private ClientCacheRepository clientCacheRepository;

    @BeforeEach
    public void setup() {
        captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        headers = new HttpHeaders();
        headers.set("Origin", ALLOWED_ORIGIN);
    }

    @Test
    public void getRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost")
                .headers(headers)
                .build();

        assertThatResponseContainsHeaders(request);

    }

    @Test
    public void postRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost")
                .headers(headers)
                .build();

        assertThatResponseContainsHeaders(request);

    }

    @Test
    public void optionsRequestFromAllowedOriginShouldReturnHttpStatusOkAndAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost")
                .headers(headers)
                .build();

        ServerWebExchange webExchange = assertThatResponseContainsHeaders(request);
        assertThat(webExchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void putRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .put("http://localhost")
                .headers(headers)
                .build();

        assertThatResponseContainsHeaders(request);

    }

    @Test
    public void deleteRequestFromAllowedOriginShouldReturnAccessControlHeadersInResponse() {
        MockServerHttpRequest request = MockServerHttpRequest
                .delete("http://localhost")
                .headers(headers)
                .build();

        assertThatResponseContainsHeaders(request);

    }

    public ServerWebExchange assertThatResponseContainsHeaders(MockServerHttpRequest request){
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());
        when(clientCacheRepository.findByClientId("")).thenReturn(Mono.empty());

        filter.filter(exchange, filterChain).block();

        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowOrigin()).isNotNull().isNotEmpty();
        assertThat(webExchange.getResponse().getHeaders().getVary()).contains("Origin");
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowHeaders()).containsAll(ALLOWED_HEADERS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowMethods()).containsAll(ALLOWED_METHODS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlAllowCredentials()).isEqualTo(ALLOW_CREDENTIALS);
        assertThat(webExchange.getResponse().getHeaders().getAccessControlMaxAge()).isEqualTo(MAX_AGE);

        return webExchange;
    }
}
