package com.interswitch.apigateway.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

@Data
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Project name is required")
    @Length(min = 5, max = 50, message = "Project name must be between 5 and 50 characters long")
    private String name;

    @NotNull(message = "Project type is required")
    private Type type;

    @EqualsAndHashCode.Exclude
    private Map<Env, String> clients = new LinkedHashMap<>();

    @EqualsAndHashCode.Exclude
    @DBRef(lazy = true)
    private Set<Resource> resources = new LinkedHashSet<>();

    private String owner = "";

    public Optional<Resource> getResource(String resourceId) {
        return this.resources.stream().filter(resource -> resource.getId().equals(resourceId)).findFirst();
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
    }

    public void removeResource(Resource resource) {
        this.resources.remove(resource);
    }

    public String getClientId(Env env) {
        String clientId = this.clients.get(env);
        return clientId != null ? clientId : "";
    }

    public void setClientId(String clientId, Env env) {
        this.clients.put(env, clientId);
    }

    public enum Type {
        web, mobile, other
    }
}
