package com.interswitch.apigateway.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Trace {
    private String requestId = UUID.randomUUID().toString();
    private String httpUri;
    private String httpMethod;
    private String httpPath;
    private String httpRequestParams;
    private String clientId;
    private String routeId;
    private String callerIp;
    private int callerPort;
    private String callerName;
    private String hostIp;
    private String hostName;
    private String httpStatusCode;
    private long requestDuration;
    private long durationOutsideGateway;
    private long durationWithinGateway;
}