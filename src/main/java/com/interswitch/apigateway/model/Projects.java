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
    private String projectSecret;

    @Indexed(unique = true)
    private String projectId;

    public Projects() {
    }

    public Projects(String id, String projectName, String passportId, String email, String projectSecret, String projectId) {
        this.id = id;
        this.projectName = projectName;
        this.passportId = passportId;
        this.email = email;
        this.projectSecret = projectSecret;
        this.projectId = projectId;
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

    public String getProjectSecret() {
        return projectSecret;
    }

    public void setProjectSecret(String projectSecret) {
        this.projectSecret = projectSecret;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}


