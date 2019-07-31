package com.interswitch.apigateway;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.interswitch.apigateway.model.ErrorResponse;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.CannotGetMongoDbConnectionException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import javax.net.ssl.SSLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class ErrorResponseService {

    public ErrorResponse fromException(Throwable e, ServerWebExchange exchange, int httpStatus, String errorMessage) {
        var message = errorMessage;
        var eMessage = e.getMessage();
        var response = new ErrorResponse();
        var code = httpStatus;
        var httpMethod = exchange.getRequest().getMethod();
        if (e instanceof DuplicateKeyException) {
            code = 409;
            String[] keys = StringUtils.substringsBetween(eMessage, "\"", "\"");
            String key = (httpMethod != HttpMethod.PUT) ? "' already exists." : "' cannot be modified here.";
            message = (keys != null) ? "'" + keys[0] + key : eMessage;
        }
        if (e instanceof WebExchangeBindException) {
            code = 400;
            String field = "";
            List<FieldError> fieldErrors = ((WebExchangeBindException) e).getFieldErrors();
            for (var fields : fieldErrors) {
                field = fields.getField() + " ( " + fields.getDefaultMessage() + " )" + ", " + field;
            }
            message = ((WebExchangeBindException) e).getReason() + " for fields: " + field;
        }
        if (e instanceof NotFoundException) {
            code = 404;
            String[] key = (eMessage != null) ? StringUtils.substringsBetween(eMessage, "\"", "\"") : null;
            message = (key[0] != null) ? key[0] : "Not Found";
        }
        if (e instanceof DecodingException) {
            code = 400;
            message = "Failed to read Http Message";
        }
        if (e instanceof ServerWebInputException) {
            code = 400;
            String[] keys = StringUtils.substringsBetween(eMessage, "(", ")");
            String key = (keys != null) ? "'" + keys[0] : "";
            message = "Failed to read Http Message: cannot deserialize Enum instance " + key;
        }
        if (e instanceof SocketException || e instanceof SSLException || e instanceof MismatchedInputException || e instanceof CannotGetMongoDbConnectionException) {
            code = 503;
            message = "Either remote server cannot be reached or network connection was reset/broken";
        }
        if (e instanceof ResourceAccessException) {
            message = e.getMessage();
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
        response.setStatus(code);
        response.setMessage(message);
        response.setPath(exchange.getRequest().getPath().toString());
        response.setError(HttpStatus.valueOf(code));
        return response;
    }
}
