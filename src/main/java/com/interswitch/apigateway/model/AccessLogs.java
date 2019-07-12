package com.interswitch.apigateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String client;

    @NotNull
    private String api;

    @NotNull
    private Entity entity;

    @NotNull
    private String entityId = "";

    @NotNull
    private Action action;

    @NotNull
    private String status;

    @JsonIgnore
    @NotNull
    private State state ;

    @NotNull
    private String summary;

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
        CREATION("POST"),
        UPDATE("PUT"),
        DELETION("DELETE");

        private String value;

        public String getValue(){
            return value;
        }

        Action(String value){
            this.value = value;
        }
    }

    public enum State {
        SUCCESSFUL, FAILED
    }
}
