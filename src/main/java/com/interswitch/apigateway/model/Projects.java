package com.interswitch.apigateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
public class Projects {
    @Id
    private String id =UUID.randomUUID().toString();
    private String projectName;
    private String passportId;
    private String email;
    private String appSecret;

    @Indexed(unique = true)
    private String appId;

    public Projects() {
    }

    public Projects(String id, String projectName, String passportId, String email, String appSecret, String appId) {
        this.id = id;
        this.projectName = projectName;
        this.passportId = passportId;
        this.email = email;
        this.appSecret = appSecret;
        this.appId = appId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getPassportId() {
        return passportId;
    }

    public void setPassportId(String passportId) {
        this.passportId = passportId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}


