package com.interswitch.apigateway.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.net.URI;

@Document(collection = "routeConfig")
@Data
public class Config {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Route id is required.")
    private String routeId;

    @EqualsAndHashCode.Exclude
    private URI sandbox;

    @EqualsAndHashCode.Exclude
    private URI uat;

}
