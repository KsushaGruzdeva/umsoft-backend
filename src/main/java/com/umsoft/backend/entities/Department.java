package com.umsoft.backend.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @OneToMany(mappedBy = "assignedDepartment")
    private List<Request> requests = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    private List<Category> categories = new ArrayList<>();

    public Department() {}

    public Department(String name, String description, String emailAddress) {
        this.name = name;
        this.description = description;
        this.emailAddress = emailAddress;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}