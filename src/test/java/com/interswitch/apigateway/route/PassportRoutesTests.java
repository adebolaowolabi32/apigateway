package com.interswitch.apigateway.route;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PassportRoutesTests {
    MultiValueMap formData = new LinkedMultiValueMap();
    @Autowired
    private WebTestClient webClient;
    private String clientId = "IKIAC4420D13ABE57D778FB7263A17D13B60A8AE4135";
    private String clientSecret = "secret";
    private String credentials = clientId + ":" + clientSecret;

    @Test
    public void testTokenEndpoint() {
        formData.setAll(Map.of("grant_type", "client_credentials", "scope", "profile"));
        this.webClient.post().uri("/oauth/token")
                .header("Authorization", format("Basic %s", new String(Base64.getEncoder().encode(credentials.getBytes()))))
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
