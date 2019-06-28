package com.interswitch.apigateway.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FilterUtil {

    public JWT DecodeBearerToken(HttpHeaders headers) {
        JWT jwtToken= null;
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
    public List<String> GetAudience(JWT jwtToken){
        List<String> audience = new ArrayList<>();
        try {
            Object aud = jwtToken.getJWTClaimsSet().getClaim("aud");
            if (aud != null) audience = (List<String>) aud;
        }
        catch (ParseException e){
            Mono.error(e).log();
        }
        return audience;

    }

    public String GetEnvironment(JWT jwtToken){
        String environment = "";
        try {
            Object env = jwtToken.getJWTClaimsSet().getClaim("env");
            if (env != null) environment = env.toString();
            return environment;
        }
        catch (ParseException e){
            Mono.error(e).log();
        }
        return environment;


    }

}
