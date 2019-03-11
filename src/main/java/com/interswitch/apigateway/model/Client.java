package com.interswitch.apigateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Document
public class Client implements Serializable {
    @Id
    private String id = UUID.randomUUID().toString();

    private String clientId;

    private List origins;

    private List resourceIds;

    public Client() {
    }

    public Client(String id, String clientId, List origins, List resourceIds) {
        this.id = id;
        this.clientId = clientId;
        this.origins = origins;
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

    public List getOrigins() { return origins; }

    public void setOrigins(List origins) { this.origins = origins; }

    public List getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List resourceIds) {
        this.resourceIds = resourceIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;
        Client client = (Client) o;
        return Objects.equals(getId(), client.getId()) &&
                Objects.equals(getClientId(), client.getClientId()) &&
                Objects.equals(getOrigins(), client.getOrigins()) &&
                Objects.equals(getResourceIds(), client.getResourceIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getClientId(), getOrigins(), getResourceIds());
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", clientId='" + clientId + '\'' +
                ", origins=" + origins +
                ", resourceIds=" + resourceIds +
                '}';
    }
}


