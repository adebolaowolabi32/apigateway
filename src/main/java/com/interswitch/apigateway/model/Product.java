package com.interswitch.apigateway.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Document
public class Product {
    @Id
    private String id = UUID.randomUUID().toString();

    @Indexed(unique = true)
    private String productId = UUID.randomUUID().toString();

    private String productName;

    private String productDescription;

    private List resourceIds;

    public  Product(){}

    public Product(String id, String productId, List resourceIds, String productName, String productDescription) {
        this.id = id;
        this.productName = productName;
        this.productDescription=productDescription;
        this.productId = productId;
        this.resourceIds = resourceIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List resourceIds) {
        this.resourceIds = resourceIds;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(getId(), product.getId()) &&
                Objects.equals(getProductName(), product.getProductName()) &&
                Objects.equals(getProductDescription(), product.getProductDescription()) &&
                Objects.equals(getResourceIds(), product.getResourceIds()) &&
                Objects.equals(getProductId(), product.getProductId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProductName(), getProductDescription(), getResourceIds(), getProductId());
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", resourceIds=" + resourceIds +
                ", productId='" + productId + '\'' +
                '}';
    }
}
