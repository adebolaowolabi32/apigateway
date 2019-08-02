package com.interswitch.apigateway.model;

import lombok.Data;

@Data
public class Trace {
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
    private String hostPort;
    private String hostName;
    private String httpStatusCode;
    private long duration;
}