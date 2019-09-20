package com.interswitch.apigateway.model;

import lombok.Data;

import java.util.*;

@Data
public class PassportClient {

    private String clientName;
    private String clientId;
    private String clientSecret;
    private Set<String> scope = Collections.emptySet();
    private Set<String> resourceIds = Collections.emptySet();
    private Set<String> authorizedGrantTypes = Collections.emptySet();
    private Set<String> autoApproveScopes = Collections.emptySet();
    private List<String> authorities = Collections.emptyList();
    private Integer accessTokenValiditySeconds = 1800;
    private Integer refreshTokenValiditySeconds = 1209600;
    private Map<String, Object> additionalInformation = new LinkedHashMap<>();
    private Set<String> registeredRedirectUri = Collections.emptySet();
    private String description;
    private String logoUrl;
    private String clientOwner;
}
