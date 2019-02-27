package com.interswitch.apigateway.route;

import com.interswitch.apigateway.config.RouteConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
@Import(RouteConfig.class)
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
        this.webClient.post().uri("/passport/oauth/token")
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
