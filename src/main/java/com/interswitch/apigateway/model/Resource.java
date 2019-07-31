package com.interswitch.apigateway.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpMethod;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Document(collection = "resources")
@Data
public class Resource {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Resource Name is Required")
    @Length(min = 5, max = 50, message = "Resource Name must be between 5 and 50 characters long")
    private String name;

    @EqualsAndHashCode.Exclude
    @NotNull(message = "Method is Required")
    private HttpMethod method;

    @EqualsAndHashCode.Exclude
    @NotBlank(message = "Path is Required")
    @Pattern(regexp = "^\\/.+$")
    private String path;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @DBRef(lazy = true)
    @JsonBackReference
    private Product product;
}
