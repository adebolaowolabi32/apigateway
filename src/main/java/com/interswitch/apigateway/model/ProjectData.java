package com.interswitch.apigateway.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

@Data
public class ProjectData {

    private String id;

    @NotBlank(message = "Project name is required")
    @Length(min = 5, max = 50, message = "Project name must be between 5 and 50 characters long")
    private String name;

    @NotNull(message = "Project type is required")
    private Project.Type type;

    private String description;

    @NotEmpty(message = "At least one authorized grant type is required")
    private Set<GrantType> authorizedGrantTypes = Collections.emptySet();

    private Set<String> registeredRedirectUris = Collections.emptySet();

    private String logoUrl = "";

    private Map<Env, String> clients = new LinkedHashMap<>();

    private String owner = "";

    private Set<Resource> resources = new LinkedHashSet<>();

}
