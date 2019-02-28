package com.interswitch.apigateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document
public class ClientResources {
    @Id
    private String Id =UUID.randomUUID().toString();
    private List resourceIds;

    @Indexed(unique = true)
    private String clientId;

    public ClientResources() {
    }

    public ClientResources(String id, String clientId, List resourceIds) {
        Id = id;
        this.clientId = clientId;
        this.resourceIds = resourceIds;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
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


