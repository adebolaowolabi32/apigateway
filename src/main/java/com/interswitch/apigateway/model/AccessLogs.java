package com.interswitch.apigateway.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document(collection = "accessLogs")
@Data
public class AccessLogs {
    @Id
    private String id;

    @NotNull
    private String username;

    @NotNull
    private String api;

    @NotNull
    private Entity entity;

    private String entityId;

    @NotNull
    private Action action;

    @NotNull
    private HttpStatus status;

    @NotNull
    private LocalDateTime timestamp = LocalDateTime.now();

    public enum Entity {
        ROUTE("/routes"),
        RESOURCE("/resources"),
        CLIENT("/clients"),
        PRODUCT("/products"),
        USER("/users");

        private String value;

        public String getValue(){
            return value;
        }

        Entity(String value){
            this.value = value;
        }

    }

    public enum Action {
        VIEWED("GET"),
        CREATED("POST"),
        UPDATED("PUT"),
        DELETED("DELETE");

        private String value;

        public String getValue(){
            return value;
        }

        Action(String value){
            this.value = value;
        }
    }
}
