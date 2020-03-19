package com.interswitch.apigateway.model;

import java.util.Collections;
import java.util.Set;

public class Endpoints {
    public static final Set<String> noAuthEndpoints = Collections.unmodifiableSet(Set.of("(?i)/actuator/health/?", "(?i)/actuator/prometheus/?", "(?i)/passport/oauth.*", "(?i).*/login/?", "(?i).*/logout/?", "(?i).*/register/?", "(?i).*/signup/?", "(?i).*/signin/?", "(?i).*/signout/?", "(?i).*/index/?", "(?i).*/home/?", "(?i).*/oauth/token/?", "(?i).*/oauth/authenticate/?", "(?i).*/oauth/authorize/?", "(?i).*/reset-password/?"));

    public static final Set<String> devEndpoints = Collections.unmodifiableSet(Set.of("/projects.*", "/golive/request.*"));

    public static final Set<String> adminEndpoints = Collections.unmodifiableSet(Set.of("/users.*", "/golive/approve.*", "/golive/decline.*", "/golive/pending.*"));

    public static final String PASSPORT_ROUTE_ID = "passport";
}
