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
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Document(collection = "products")
@Data
public class Product {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Product name is Required")
    @Length(min = 5, max = 50, message = "Product name must be between 5 and 50 characters long")
    private String name;

    @EqualsAndHashCode.Exclude
    @NotNull
    @Length(max = 500, message = "Product description must be less than 500 characters long")
    private String description = "";

    @EqualsAndHashCode.Exclude
    @NotBlank(message = "Documentation URL is Required")
    @URL(message = "Documentation URL is not valid")
    @Length(max = 500, message = "Documentation URL must be less than 500 characters long")
    private String documentation;

    @EqualsAndHashCode.Exclude
    @NotNull
    private Category category = Category.PUBLIC;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @DBRef(lazy = true)
    private Set<Resource> resources = new LinkedHashSet<>();

    @EqualsAndHashCode.Exclude
    private Set<String> audiences = new LinkedHashSet<>();

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @DBRef(lazy = true)
    @JsonBackReference
    private Set<Project> projects = new LinkedHashSet<>();

    public void addResource(Resource resource){
        resources.add(resource);
    }

    public void removeResource(Resource resource){
        resources.remove(resource);
    }

    public void addProject(Project project) {
        projects.add(project);
    }

    public void removeProject(Project project) {
        projects.remove(project);
    }

    public enum Category {
        PUBLIC, RESTRICTED
    }
}
