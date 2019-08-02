package com.interswitch.apigateway.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public final class FilterUtil {

    public static JWT decodeBearerToken(HttpHeaders headers) {
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
        return jwtToken;
    }

    public static String getClaimAsStringFromBearerToken(JWT jwtToken, String claim) {
        String claimAsString = "";
        try {
            Object claimObject = jwtToken.getJWTClaimsSet().getClaim(claim);
            if (claimObject != null) claimAsString = claimObject.toString().trim();
        } catch (ParseException e) {
            Mono.error(e).log();
        }
        return claimAsString;
    }

    public static List<String> getClaimAsListFromBearerToken(JWT jwtToken, String claim) {
        List<String> claimAsList = new ArrayList<>();
        try {
            Object claimObject = jwtToken.getJWTClaimsSet().getClaim(claim);
            if (claimObject != null) claimAsList = (List<String>) claimObject;
        } catch (ParseException e) {
            Mono.error(e).log();
        }
        return claimAsList;
    }
}