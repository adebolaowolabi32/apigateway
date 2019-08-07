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
    private Action action;

    @NotNull
    private Status status;

    @NotNull
    private LocalDateTime timestamp = LocalDateTime.now();

    public enum Entity {
        ROUTE("/routes"),
        RESOURCE("/resources"),
        CLIENT("/clients"),
        PRODUCT("/products"),
        USER("/users"),
        SYSTEM("/actuator"),
        ENVIRONMENT("/environment");


        private String value;

        public String getValue(){
            return value;
        }

        Entity(String value){
            this.value = value;
        }

    }

    public enum Action {
        CREATE("POST"),
        UPDATE("PUT"),
        DELETE("DELETE"),
        REFRESH("REFRESH");

        private String value;

        public String getValue(){
            return value;
        }

        Action(String value){
            this.value = value;
        }
    }

    public enum Status {
        SUCCESSFUL, FAILED
    }

    public enum ActuatorEndpoint {
        ROUTE_REFRESH("/actuator/gateway/refresh"),
        BUS_REFRESH("/actuator/bus-refresh");

        private String value;

        public String getValue(){
            return value;
        }

        ActuatorEndpoint(String value){
            this.value = value;
        }
    }
}
