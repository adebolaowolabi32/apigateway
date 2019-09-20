package com.interswitch.apigateway.route;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class PassportRoutesTests {
    MultiValueMap formData = new LinkedMultiValueMap();

    @Autowired
    private WebTestClient webClient;

    @Value("${client.id.test}")
    private String clientId;
    @Value("${client.secret.test}")
    private String clientSecret;

    @Test
    public void testTokenEndpoint() {
        formData.setAll(Map.of("grant_type", "client_credentials", "scope", "profile"));
        this.webClient.post().uri("/passport/oauth/token?env=TEST")
                .headers(h -> h.setBasicAuth(clientId, clientSecret, Charset.forName("UTF-8")))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(fromFormData(formData))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.access_token")
                .isNotEmpty();
    }
}
