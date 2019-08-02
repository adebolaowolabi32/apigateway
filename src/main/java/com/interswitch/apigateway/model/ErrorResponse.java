package com.interswitch.apigateway.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErrorResponse {
    @NotNull
    private LocalDateTime timestamp = LocalDateTime.now();

    @NotNull
    private Integer status;

    @NotNull
    private String message;

    private List<String> errors;
}

