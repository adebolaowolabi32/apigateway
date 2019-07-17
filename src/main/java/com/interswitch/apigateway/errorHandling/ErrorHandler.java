package com.interswitch.apigateway.errorHandling;

import com.interswitch.apigateway.exceptions.NotExistException;
import com.interswitch.apigateway.model.ErrorResponse;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import net.logstash.logback.encoder.org.apache.commons.lang.WordUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> duplicateErrorException(final DuplicateKeyException e, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        String message = e.getMessage();
        String[] key = StringUtils.substringsBetween(message, "\"", "\"");
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse response = new ErrorResponse(path, status.value(), WordUtils.capitalize(status.name().toLowerCase()), "'" + key[0] + "' already exists.");
        return handleExceptionInternal(response, status);
    }

    @ExceptionHandler(NotExistException.class)
    public ResponseEntity<ErrorResponse> doesNotExist(final NotExistException e, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse response = new ErrorResponse(path, status.value(), WordUtils.capitalize(status.name().toLowerCase()), e.getMessage());
        return handleExceptionInternal(response, status);
    }



    protected ResponseEntity<ErrorResponse> handleExceptionInternal(ErrorResponse error, HttpStatus status) {
        return new ResponseEntity<>(error, status);
    }


}
