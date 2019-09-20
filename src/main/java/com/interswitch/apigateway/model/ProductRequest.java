package com.interswitch.apigateway.model;

import lombok.Data;

import java.util.Set;

@Data
public class ProductRequest {

    private String name;

    private String description;

    private Set<ResourceRequest> resources;

}
