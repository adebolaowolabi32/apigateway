package com.interswitch.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.nio.charset.Charset;
import java.util.Map;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ClientResourcesCacheTests {
    @Autowired
    private WebTestClient webClient;
    private String clientId = "IKIAC4420D13ABE57D778FB7263A17D13B60A8AE4135";
    private String clientSecret = "secret";
    private String credentials = clientId + ":" + clientSecret;

    @Test
    public void testTokenEndpoint() {
        this.webClient.get().uri("/")
                .headers(h -> h.setBasicAuth(clientId, clientSecret, Charset.forName("UTF-8")))
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.access_token")
                .isNotEmpty();
    }
}