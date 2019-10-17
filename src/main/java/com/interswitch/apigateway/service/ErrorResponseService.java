package com.interswitch.apigateway.service;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.interswitch.apigateway.model.ErrorResponse;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.CannotGetMongoDbConnectionException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ErrorResponseService {

    public ErrorResponse fromException(Throwable e, ServerWebExchange exchange, int httpStatus, String errorMessage) {
        var message = errorMessage;
        var eMessage = e.getMessage();
        var response = new ErrorResponse();
        var code = httpStatus;
        var httpMethod = exchange.getRequest().getMethod();
        ArrayList<String> errors = new ArrayList<>();
        if (e instanceof DuplicateKeyException) {
            code = 409;
            String[] keys = StringUtils.substringsBetween(eMessage, "\"", "\"");
            String key = (httpMethod != HttpMethod.PUT) ? " already exists." : " cannot be modified here.";
            message = (keys != null) ? keys[0] + key : message;
        }
        if (e instanceof WebExchangeBindException || e instanceof ServerWebInputException) {
            code = 400;
            if (e instanceof WebExchangeBindException) {
                List<FieldError> fieldErrors = ((WebExchangeBindException) e).getFieldErrors();
                String error = ((WebExchangeBindException) e).getReason();
                String field = "";
                StringBuilder messageError = new StringBuilder();

                for(int i = 0; i < fieldErrors.size(); i++){
                    var fieldError = fieldErrors.get(i);
                    if (i == 0) messageError.append(fieldError.getDefaultMessage());
                    else {
                        messageError.append(", ");
                        messageError.append(fieldError.getDefaultMessage());
                    }
                    field = fieldError.getField() + " (" + fieldError.getDefaultMessage() + ")";
                    errors.add(error + " for field: " + field);
                }
                message = messageError.toString();
            } else {
                eMessage = ((ServerWebInputException) e).getMessage();
                String[] keys = StringUtils.substringsBetween(eMessage, "(", ")");
                String[] key = (keys != null) ? StringUtils.substringsBetween(keys[0], "\"", "\"") : StringUtils.substringsBetween(eMessage, "\"", "\"");
                String field = "Bad Input.";
                if (key != null)
                    field = (key[0].contains("mismatch")) ? "Data Type mismatch for value " + key[1] : "Invalid value for field " + key[0];
                message = "Validation failure: " + field;
            }
        }
        if (e instanceof NotFoundException) {
            code = 404;
            String[] key = (eMessage != null) ? StringUtils.substringsBetween(eMessage, "\"", "\"") : null;
            message = (key[0] != null) ? key[0] : "Not Found";
        }

        if (e instanceof SocketException || e instanceof SSLException || e instanceof MismatchedInputException || e instanceof CannotGetMongoDbConnectionException || e instanceof RestClientException || e instanceof ConnectException) {
            code = 503;
            message = "Either remote server cannot be reached or network connection was reset/broken. Please try again later";
        }
        if (e instanceof ResourceAccessException) {
            message = "Something went wrong, please bear with us while we fix it.";
        }
        if (e instanceof UnknownHostException) {
            message = "Unknown Host. Host Ip could not be determined.";
        }
        if (e instanceof HttpServerErrorException.InternalServerError) {
            code = 500;
            message = "An Unexpected error has occurred. Please try again later.";
        }
        if (e instanceof MethodNotAllowedException) {
            code = 415;
        }
        if (e instanceof IllegalArgumentException) {
            code = 400;
        }
        response.setStatus(code);
        response.setMessage(message);
        response.setErrors(errors);
        return response;
    }
}
