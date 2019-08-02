package com.interswitch.apigateway.errorHandling;

import com.interswitch.apigateway.service.ErrorResponseService;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component
public class ErrorAttributes extends DefaultErrorAttributes {

    private ErrorResponseService errorResponseService;

    public ErrorAttributes(ErrorResponseService errorResponseService) {
        this.errorResponseService = errorResponseService;
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  boolean includeStackTrace) {
        var errorAttributes = super.getErrorAttributes(request, includeStackTrace);
        errorAttributes.put("timestamp", new Date());
        Throwable error = getError(request);
        if (Objects.nonNull(error)) {
            int status = Integer.parseInt(errorAttributes.get("status").toString());
            var errorMessage = errorAttributes.get("error").toString();
            var response = errorResponseService.fromException(error, request.exchange(), status, errorMessage);
            errorAttributes.put("status", response.getStatus());
            errorAttributes.put("message", response.getMessage());
            if (!response.getErrors().isEmpty()) {
                errorAttributes.put("errors", response.getErrors());
            } else {
                errorAttributes.remove("errors");
            }
            errorAttributes.remove("error");
            errorAttributes.remove("path");
        }
        return errorAttributes;
    }

}
