package com.interswitch.apigateway.errorHandling;

import com.interswitch.apigateway.model.ErrorResponse;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import net.logstash.logback.encoder.org.apache.commons.lang.WordUtils;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> duplicateErrorException(final DuplicateKeyException e, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        HttpMethod httpMethod = exchange.getRequest().getMethod();
        String message = (httpMethod != HttpMethod.PUT) ? "' already exists." : "' cannot be modified here.";
        String eMessage = e.getMessage();
        String[] keys = StringUtils.substringsBetween(eMessage, "\"", "\"");
        String key = (keys != null) ? "'" + keys[0] + message : eMessage;
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse response = new ErrorResponse(path, status.value(), WordUtils.capitalize(status.name().toLowerCase()), key);
        return handleExceptionInternal(response, status);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> unsupportedMediaType(final WebExchangeBindException e, ServerWebExchange exchange) {
        String field = "";
        List<FieldError> fieldErrors = e.getFieldErrors();
        for (var fields : fieldErrors) {
            field = fields.getField() + " ( " + fields.getDefaultMessage() + " )" + ", " + field;
        }
        String path = exchange.getRequest().getPath().toString();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse response = new ErrorResponse(path, status.value(), WordUtils.capitalize(status.name().toLowerCase()), e.getReason() + " for fields: " + field);
        return handleExceptionInternal(response, status);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundException(final NotFoundException e, ServerWebExchange exchange) {
        String message = e.getMessage();
        String path = exchange.getRequest().getPath().toString();
        String[] key = (message != null) ? StringUtils.substringsBetween(message, "\"", "\"") : null;
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse response = new ErrorResponse(path, status.value(), WordUtils.capitalize(status.name().toLowerCase()), key[0]);
        return handleExceptionInternal(response, status);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleThrowable(final Throwable ex, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse response = new ErrorResponse(path, status.value(), WordUtils.capitalize(status.name().toLowerCase()), "An unexpected server error has occurred, please try again later.");
        return handleExceptionInternal(response, status);
    }


    protected ResponseEntity<ErrorResponse> handleExceptionInternal(ErrorResponse error, HttpStatus status) {
        return new ResponseEntity<>(error, status);
    }


}
