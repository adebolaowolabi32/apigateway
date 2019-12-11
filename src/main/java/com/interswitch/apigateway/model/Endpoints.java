package com.interswitch.apigateway.model;

import java.util.Collections;
import java.util.Set;

public class Endpoints {
    public static final Set<String> noAuthEndpoints = Collections.unmodifiableSet(Set.of("/passport/oauth.*", ".*/login/?", ".*/logout/?", ".*/register/?", ".*/signup/?", ".*/signin/?", ".*/signout/?", ".*/index/?", ".*/home/?", ".*/oauth/token/?", ".*/oauth/authenticate/?", ".*/oauth/authorize/?", ".*/reset-password/?", ".*/finch/user-mgmt/auth/users/oauth/authenticate/otp/?", ".*/finch/user-mgmt/auth/users/refreshUserToken/?", ".*/finch/user-mgmt/auth/users/generateToken/?"));

    public static final Set<String> noAuthSystemEndpoints = Collections.unmodifiableSet(Set.of("/actuator/health/?", "/actuator/prometheus/?"));

    public static final Set<String> devEndpoints = Collections.unmodifiableSet(Set.of("/projects.*", "/golive/request.*"));

    public static final Set<String> adminEndpoints = Collections.unmodifiableSet(Set.of("/users.*", "/golive/approve.*", "/golive/decline.*", "/golive/pending.*"));

    public static final String GATEWAY_SAVE_URL = "/actuator/gateway/routes/";
    
    public static final String PASSPORT_ROUTE_ID = "passport";
}
