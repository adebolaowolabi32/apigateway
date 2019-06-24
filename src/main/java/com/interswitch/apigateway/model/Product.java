package com.interswitch.apigateway.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "products")
@Data
public class Product {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Name is Required")
    @Length(min = 5, max = 50, message = "Name must be between 5 and 50 characters long")
    private String name;

    @EqualsAndHashCode.Exclude
    @Length(max = 500, message = "Description must less than 500 characters long")
    private String description;

    @EqualsAndHashCode.Exclude
    @URL(message = "Documentation URL not valid")
    @Length(max = 500, message = "Documentation URL must less than 500 characters long")
    private String documentation;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @DBRef(lazy = true)
    private List<Resource> resources = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @DBRef(lazy = true)
    @JsonBackReference
    private List<Client> clients = new ArrayList<>();

    public void addResource(Resource resource){
        resources.add(resource);
    }

    public void removeResource(Resource resource){
        resources.remove(resource);
    }

    public void addClient(Client client){
        clients.add(client);
    }

    public void removeClient(Client client){
        clients.remove(client);
    }
}
