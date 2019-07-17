package com.interswitch.apigateway.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
public class ErrorResponse {

    @NotNull
    private ZonedDateTime timestamp = ZonedDateTime.now();

    @NotNull
    private String path;

    @NotNull
    private Integer status;

    @NotNull
    private String error;

    @NotNull
    private String message;


    public ErrorResponse(@NotNull String path, @NotNull Integer status, @NotNull String error, @NotNull String message) {
        this.path = path;
        this.status = status;
        this.error = error;
        this.message = message;
    }
}

