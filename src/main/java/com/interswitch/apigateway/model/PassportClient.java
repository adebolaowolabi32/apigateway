package com.interswitch.apigateway.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotBlank;
import java.util.*;

@Data
public class PassportClient {

    @NotBlank(message = "Client Name is Required")
    @Length(min = 5, max = 50, message = "Client ID must be between 5 and 50 characters long")
    private String clientName;

    private String clientId;
    private String clientSecret;
    private Set<String> scope = Collections.emptySet();
    private Set<String> resourceIds = Collections.emptySet();
    private Set<GrantType> authorizedGrantTypes = Collections.emptySet();
    private Set<String> autoApproveScopes = Collections.emptySet();
    private List<GrantedAuthority> authorities = Collections.emptyList();
    private Integer accessTokenValiditySeconds = 1800;
    private Integer refreshTokenValiditySeconds = 1209600;
    private Map<String, Object> additionalInformation = new LinkedHashMap<>();
    private Set<String> registeredRedirectUri = Collections.emptySet();
    private String description;
    private String logoUrl;
    private String clientOwner;
}
