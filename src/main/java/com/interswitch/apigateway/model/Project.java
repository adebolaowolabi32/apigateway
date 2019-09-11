package com.interswitch.apigateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

@Data
@Document(collection = "projects")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Project name is required")
    @Length(min = 5, max = 50, message = "Project name must be between 5 and 50 characters long")
    private String name;

    @NotNull(message = "Project type is required")
    private Type type;

    private String description;

    @NotEmpty(message = "At least one authorized grant type is required")
    private Set<GrantType> authorizedGrantTypes = Collections.emptySet();

    private Set<String> registeredRedirectUris = Collections.emptySet();

    private String logoUrl = "";

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Map<Env, String> clients = new LinkedHashMap<>();

    private String owner = "";

    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @DBRef(lazy = true)
    private Set<Product> products = new LinkedHashSet<>();

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
    }

    public String getClientId(Env env) {
        String clientId = this.clients.get(env);
        return clientId != null ? clientId : "";
    }

    public void setClientId(String clientId, Env env) {
        Map<Env, String> clients = new LinkedHashMap<>();
        clients.put(env, clientId);
        this.clients = clients;
    }

    public enum Type {
        web, mobile, other
    }
}
