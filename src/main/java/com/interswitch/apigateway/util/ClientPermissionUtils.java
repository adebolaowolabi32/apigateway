package com.interswitch.apigateway.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.List;

@Component
public class ClientPermissionUtils {
    public String GetClientIdFromBearerToken(HttpHeaders headers) {
        String client_id = "";
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokens = headers.get(HttpHeaders.AUTHORIZATION);
            if (accesstokens != null && !accesstokens.isEmpty()) {
                String accesstoken = accesstokens.get(0);
                if (accesstoken.contains("Bearer ")) {
                    accesstoken = accesstoken.replaceFirst("Bearer ", "");
                    if (!accesstoken.isEmpty()) {
                        try {
                            JWT jwtToken = JWTParser.parse(accesstoken);
                            client_id = jwtToken.getJWTClaimsSet().getClaim("client_id").toString();
                        } catch (ParseException e) {
                            Mono.error(e).log();
                        }
                    }
                }
            }
        }
        return client_id;
    }
}
