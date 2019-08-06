package com.interswitch.apigateway.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.net.URI;

@Document(collection = "routeConfig")
@Data
public class RouteConfig {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Route id is required.")
    private String routeId;

    private URI sandboxUri;

    private URI uatUri;

    public String getRouteId() {
        return routeId;
    }

    public URI getSandboxUri() {
        return sandboxUri;
    }

    public URI getUatUri() {
        return uatUri;
    }

}
