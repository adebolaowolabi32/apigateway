package com.interswitch.apigateway.service;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.model.Env;
import com.interswitch.apigateway.model.PassportClient;
import com.interswitch.apigateway.model.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class PassportService {
    private final String localIp = "http://127.0.0.1";
    private WebClient webClient;
    private WebClient.Builder webClientBuilder;
    @Value("${passport.clientEndpoint}")
    private String clientEndpoint;
    @Value("${passport.tokenEndpoint}")
    private String tokenEndpoint;

    public PassportService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public static PassportClient buildPassportClientForEnvironment(Project project, Env env) {
        PassportClient passportClient = buildPassportClient(project);
        Set<String> audiences = project.getAudiences();
        audiences.addAll(Set.of("api-gateway", "passport"));
        passportClient.setResourceIds(audiences);
        passportClient.setAdditionalInformation(Map.of("env", env));
        return passportClient;
    }

    private static PassportClient buildPassportClient(Project project) {
        PassportClient passportClient = new PassportClient();
        passportClient.setClientName(project.getName());
        passportClient.setDescription(project.getDescription());
        passportClient.setClientOwner(project.getOwner());
        passportClient.setScope(Collections.singleton("profile"));
        passportClient.setAuthorizedGrantTypes(project.getAuthorizedGrantTypes());
        passportClient.setRegisteredRedirectUri(project.getRegisteredRedirectUris());
        passportClient.setLogoUrl(project.getLogoUrl());
        int accessTokenValiditySeconds;
        int refreshTokenValiditySeconds;
        if (project.getType().equals(Project.Type.web)) {
            accessTokenValiditySeconds = 1800;
            refreshTokenValiditySeconds = 3600;
        } else {
            accessTokenValiditySeconds = 3600;
            refreshTokenValiditySeconds = 7200;
        }
        passportClient.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
        passportClient.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
        return passportClient;
    }

    public void buildWebClient(int port) {
        this.webClient = this.webClientBuilder.baseUrl(this.localIp + ":" + port).build();
    }

    public Mono<PassportClient> getPassportClient(String clientId, String accessToken, Env env) {
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
    }

    public Mono<PassportClient> createPassportClient(PassportClient client, String accessToken, Env env) {
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
    }

    public Mono<Void> updatePassportClient(PassportClient client, String accessToken, Env env) {
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
    }

    public Mono<Object> getAccessToken(MultiValueMap formData, Client client, Env env) {
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
                .bodyToMono(Object.class);
    }

    private String addEnvQueryParam(Env env) {
        if (env.equals(Env.TEST)) return "?env=TEST";
        return "";
    }
}
