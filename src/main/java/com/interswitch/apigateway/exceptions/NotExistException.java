package com.interswitch.apigateway.exceptions;

public class NotExistException extends Exception {
    String resource;

    private NotExistException(String resource) {
        this.resource = resource;
    }

    public static NotExistException createWith(String resource) {
        return new NotExistException(resource);
    }

    public String getMessage() {
        return "'" + resource + "' does not exist.";
    }
}
