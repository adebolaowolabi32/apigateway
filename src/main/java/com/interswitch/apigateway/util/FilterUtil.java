package com.interswitch.apigateway.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class FilterUtil {

    public static JWT decodeBearerToken(HttpHeaders headers) {
        String accesstoken = "";
        JWT jwtToken = null;
        if (headers != null && headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            List<String> accesstokens = headers.get(HttpHeaders.AUTHORIZATION);
            if (accesstokens != null && !accesstokens.isEmpty()) {
                accesstoken = accesstokens.get(0);
            }
        }
        if (accesstoken != null && accesstoken.contains("Bearer ")) {
            accesstoken = accesstoken.replaceFirst("Bearer ", "");
            if (!accesstoken.isEmpty()) {
                try {
                    jwtToken = JWTParser.parse(accesstoken);
                } catch (ParseException e) {
                    Mono.error(e).log();
                }
            }
        }
        return jwtToken;
    }


    public static String getClaimAsStringFromBearerToken(JWT jwtToken, String claim) {
        String claimAsString = "";
        if (jwtToken != null) {
            try {
                Object claimObject = jwtToken.getJWTClaimsSet().getClaim(claim);
                if (claimObject != null) {
                    claimAsString = claimObject.toString().trim();
                    if (!claim.equals("client_id")) claimAsString = claimAsString.toLowerCase();
                }
            } catch (ParseException e) {
                Mono.error(e).log();
            }
        }
        return claimAsString;
    }

    public static List<String> getClaimAsListFromBearerToken(JWT jwtToken, String claim) {
        List<String> claimAsList = new ArrayList<>();
        if (jwtToken != null) {
            try {
                Object claimObject = jwtToken.getJWTClaimsSet().getClaim(claim);
                if (claimObject != null) claimAsList = (List<String>) claimObject;
            } catch (ParseException e) {
                Mono.error(e).log();
            }
        }
        return claimAsList;
    }

    public static boolean isInterswitchEmail(String email) {
        return email.endsWith("@interswitchgroup.com") ||
                email.endsWith("@interswitch.com") ||
                email.endsWith("@interswitchng.com");
    }

    public static boolean match(String path, Set<String> paths) {
        boolean match = false;
        for (var p : paths) {
            if (path.matches(p)) {
                match = true;
                break;
            }
        }
        return match;
    }
}