package com.interswitch.apigateway.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Document(collection = "routeEnvironments")
@Data
public class RouteEnvironment {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Route ID is required")
    private String routeId;

    @EqualsAndHashCode.Exclude
    @Pattern(regexp = "^https?:\\/\\/.+$", message = "Test URL must be a valid URL pattern")
    private String testURL;
}
