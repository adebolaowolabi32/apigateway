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

    private URI sandbox;

    private URI uat;

    public String getRouteId() {
        return routeId;
    }

    public URI getSandbox() {
        return sandbox;
    }

    public URI getUat() {
        return uat;
    }

}
