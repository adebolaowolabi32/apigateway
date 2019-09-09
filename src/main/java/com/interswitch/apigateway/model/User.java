package com.interswitch.apigateway.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Username is Required")
    @Length(min = 5, max = 50, message = "Username must be between 5 and 50 characters long")
    private String username;

    @EqualsAndHashCode.Exclude
    @NotNull
    private Role role = Role.USER;

    public enum Role {
        ADMIN, USER
    }
}
