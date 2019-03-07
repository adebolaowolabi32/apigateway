package com.interswitch.apigateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Document
public class ClientResources implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();

    @Indexed(unique = true)
    private String clientId;

    private List resourceIds;

    public ClientResources() {
    }

    public ClientResources(String id, String clientId, List resourceIds) {
        this.id = id;
        this.clientId = clientId;
        this.resourceIds = resourceIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List resourceIds) {
        this.resourceIds = resourceIds;
    }
}


