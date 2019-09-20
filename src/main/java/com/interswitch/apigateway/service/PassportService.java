package com.interswitch.apigateway.service;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.PassportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Map;

@Service
public class PassportService {
    private final String localIp = "http://127.0.0.1";
    private WebClient webClient;
    private WebClient.Builder webClientBuilder;
    @Value("${passport.clientEndpoint}")
    private String clientEndpoint;
    @Value("${passport.tokenEndpoint}")
    private String tokenEndpoint;
    @Value("${client.id.test}")
    private String testClientId;
    @Value("${client.id.live}")
    private String liveClientId;
    @Value("${client.secret.test}")
    private String testClientSecret;
    @Value("${client.secret.live}")
    private String liveClientSecret;

    public PassportService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public void buildWebClient(int port) {
        this.webClient = this.webClientBuilder.baseUrl(this.localIp + ":" + port).build();
    }

    public Flux<PassportClient> getPassportClients(String clientOwner, Env env) {
        return getAccessToken(env).flatMapMany(accesstoken -> {
            return webClient
                    .get()
                    .uri(clientEndpoint + addEnvQueryParam(env) + "&clientOwner=" + clientOwner)
                    .header(HttpHeaders.AUTHORIZATION, accesstoken)
                    .retrieve()
                    .onStatus(HttpStatus::is1xxInformational, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve client from Passport service")))
                    .onStatus(HttpStatus::is3xxRedirection, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve client from Passport service")))
                    .onStatus(HttpStatus::isError, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve client from Passport service")))
                    .bodyToFlux(PassportClient.class);
        });
    }


    public Mono<PassportClient> getPassportClient(String clientId, Env env) {
        return getAccessToken(env).flatMap(accessToken -> {
            return webClient
                    .get()
                    .uri(clientEndpoint + clientId + addEnvQueryParam(env))
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .retrieve()
                    .onStatus(HttpStatus::is1xxInformational, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve client from Passport service")))
                    .onStatus(HttpStatus::is3xxRedirection, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve client from Passport service")))
                    .onStatus(HttpStatus::isError, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve client from Passport service")))
                    .bodyToMono(PassportClient.class);
        });
    }

    public Mono<PassportClient> createPassportClient(PassportClient client, Env env) {
        return getAccessToken(env).flatMap(accessToken -> {
            return webClient
                    .post()
                    .uri(clientEndpoint + addEnvQueryParam(env))
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .body(BodyInserters.fromObject(client))
                    .retrieve()
                    .onStatus(HttpStatus::is1xxInformational, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to create client on Passport service")))
                    .onStatus(HttpStatus::is3xxRedirection, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to create client on Passport service")))
                    .onStatus(HttpStatus::isError, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to create client on Passport service")))
                    .bodyToMono(PassportClient.class);
        });
    }

    public Mono<Void> updatePassportClient(PassportClient client, Env env) {
        return getAccessToken(env).flatMap(accessToken -> {
            return webClient
                    .put()
                    .uri(clientEndpoint + addEnvQueryParam(env))
                    .header(HttpHeaders.AUTHORIZATION, accessToken)
                    .body(BodyInserters.fromObject(client))
                    .retrieve()
                    .onStatus(HttpStatus::is1xxInformational, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to update client on Passport service")))
                    .onStatus(HttpStatus::is3xxRedirection, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to update client on Passport service")))
                    .onStatus(HttpStatus::isError, clientResponse ->
                            Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to update client on Passport service")))
                    .bodyToMono(Void.class);
        });
    }

    public Mono<String> getAccessToken(Env env) {
        MultiValueMap formData = new LinkedMultiValueMap();
        String client_id = "";
        String clientSecret = "";
        if (env.equals(Env.TEST)) {
            client_id = testClientId;
            clientSecret = testClientSecret;
        } else if (env.equals(Env.LIVE)) {
            client_id = liveClientId;
            clientSecret = liveClientSecret;
        }
        formData.setAll(Map.of("grant_type", "client_credentials", "scope", "profile+clients"));
        Client client = new Client();
        client.setClientId(client_id);
        client.setClientSecret(clientSecret);
        return webClient
                .post()
                .uri(tokenEndpoint + addEnvQueryParam(env))
                .headers(h -> h.setBasicAuth(client.getClientId(), client.getClientSecret(), Charset.forName("UTF-8")))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatus::is1xxInformational, clientResponse ->
                        Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve access token from Passport service")))
                .onStatus(HttpStatus::is3xxRedirection, clientResponse ->
                        Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve access token from Passport service")))
                .onStatus(HttpStatus::isError, clientResponse ->
                        Mono.error(new ResponseStatusException(clientResponse.statusCode(), "Failed to retrieve access token from Passport service")))
                .bodyToMono(Object.class).flatMap(result -> {
                    Map<String, Object> json = (Map<String, Object>) result;
                    return Mono.just("Bearer " + json.get("access_token").toString());
                });
    }

    private String addEnvQueryParam(Env env) {
        if (env.equals(Env.TEST)) return "?env=TEST";
        return "";
    }
}
