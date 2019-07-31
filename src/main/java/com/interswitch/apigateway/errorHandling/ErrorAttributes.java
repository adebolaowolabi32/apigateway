package com.interswitch.apigateway.errorHandling;

import com.interswitch.apigateway.ErrorResponseService;
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
        errorAttributes.put("path", request.path());
        Throwable error = getError(request);
        if (Objects.nonNull(error)) {
            var response = errorResponseService.fromException(error, request.exchange());
            errorAttributes.put("status", response.getStatus());
            errorAttributes.put("error", response.getError());
            errorAttributes.put("message", response.getMessage());
            errorAttributes.remove("errors");
        }
        return errorAttributes;
    }

}
