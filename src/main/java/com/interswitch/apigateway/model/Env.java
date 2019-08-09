package com.interswitch.apigateway.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Document(collection = "environments")
@Data
public class Env {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Route id is required.")
    private String routeId;

    @EqualsAndHashCode.Exclude
    @Pattern(regexp = "^https?:\\/\\/.+$", message = "Sandbox Url must be a valid url pattern")
    private String sandbox;

    @EqualsAndHashCode.Exclude
    @Pattern(regexp = "^https?:\\/\\/.+$", message = "UAT Url must be a valid url pattern")
    private String uat;

    public enum environment {
        UAT, TEST, SANDBOX, DEV
    }
}
