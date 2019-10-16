package com.interswitch.apigateway.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Trace {
    private String httpUri;
    private String httpMethod;
    private String httpPath;
    private String httpRequestParams;
    private String httpStatusCode;
    private String httpStatus;
    private String productName;
    private String routeId;
    private String projectName;
    private String clientId;
    private String callerIp;
    private int callerPort;
    private String callerName;
    private String hostIp;
    private String hostName;
    private long requestDuration;
    private long durationOutsideGateway;
    private long durationWithinGateway;
}