package com.interswitch.apigateway.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FilterUtil {

    public JWT decodeBearerToken(HttpHeaders headers) {
        JWT jwtToken = null;
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokens = headers.get(HttpHeaders.AUTHORIZATION);
            if (accesstokens != null && !accesstokens.isEmpty()) {
                String accesstoken = accesstokens.get(0);
                if (accesstoken.contains("Bearer ")) {
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if (!accesstoken.isEmpty()) {
                        try {
                            jwtToken = JWTParser.parse(accesstoken);
                        } catch (ParseException e) {
                            Mono.error(e).log();
                        }
                    }
                }
            }
        }
        return jwtToken ;
    }

    public List<String> getAudienceFromBearerToken(JWT jwtToken) {
        List<String> audience = new ArrayList<>();
        if (jwtToken != null) {
            try {
                Object aud = jwtToken.getJWTClaimsSet().getClaim("aud");
                if (aud != null) audience = (List<String>) aud;
            } catch (ParseException e) {
                Mono.error(e).log();
            }
        }
        return audience;
    }

    public String getEnvironmentFromBearerToken(JWT jwtToken) {
        String environment = "";
        if (jwtToken != null) {
            try {
                Object env = jwtToken.getJWTClaimsSet().getClaim("env");
                if (env != null) environment = env.toString();
            } catch (ParseException e) {
                Mono.error(e).log();
            }
        }
        return environment;
    }

    public String getClientIdFromBearerToken(JWT accessToken) {
        String client_id = "";
        if (accessToken != null) {
            try {
                Object client = accessToken.getJWTClaimsSet().getClaim("client_id").toString();
                if(client != null) client_id = client.toString();
            } catch (ParseException e) {
                Mono.error(e).log();
            }
        }
        return client_id;
    }

    public List<String> getResourcesFromBearerToken(JWT accessToken) {
        List<String> resources = new ArrayList<>();
        if (accessToken != null) {
            try {
                Object resource = accessToken.getJWTClaimsSet().getClaim("api_resources");
                if(resource != null) resources = (List<String>) resource;
            } catch (ParseException e) {
                Mono.error(e).log();
            }
        }
        return resources;
    }

    public String getUsernameFromBearerToken(JWT accessToken) {
        String username = "";
        if (accessToken != null) {
            try {
                Object user = accessToken.getJWTClaimsSet().getClaim("user_name");
                if(user != null) username = user.toString();
            } catch (ParseException e) {
                Mono.error(e).log();
            }
        }
        return username.toLowerCase();
    }
}
