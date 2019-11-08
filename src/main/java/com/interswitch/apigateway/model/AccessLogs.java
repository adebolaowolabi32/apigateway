package com.interswitch.apigateway.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private Object action;

    @NotNull
    private Status status;

    @NotNull
    private LocalDateTime timestamp = LocalDateTime.now();

    public enum Entity {
        REFRESH("refresh"),
        ROUTE("/routes"),
        RESOURCE("/resources"),
        PROJECT("/projects"),
        PRODUCT("/products"),
        USER("/users"),
        ROUTE_ENVIRONMENT("/env"),
        GOLIVE("/golive"),
        SYSTEM("/actuator");

        private String value;

        public String getValue(){
            return value;
        }

        Entity(String value){
            this.value = value;
        }

    }

    public enum MethodActions {
        CREATE("POST"),
        UPDATE("PUT"),
        DELETE("DELETE"),
        REFRESH("REFRESH");

        private String value;

        MethodActions(String value) {
            this.value = value;
        }

        public String getValue(){
            return value;
        }
    }

    public enum GoliveActions {
        REQUEST("/request"),
        APPROVE("/approve"),
        DECLINE("/decline");

        private String value;

        GoliveActions(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Status {
        SUCCESSFUL, FAILED
    }
}
