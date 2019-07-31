package com.interswitch.apigateway.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    @NotNull
    private LocalDateTime timestamp = LocalDateTime.now();

    @NotNull
    private String path;

    @NotNull
    private Integer status;

    @NotNull
    private HttpStatus error;

    @NotNull
    private String message;


//    public ErrorResponse(@NotNull String path, @NotNull Integer status, @NotNull String error, @NotNull String message) {
//        this.path = path;
//        this.status = status;
//        this.error = error;
//        this.message = message;
//    }
}

