package com.travelbooking.catalogue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "travel_packages")
public class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageId;

    @NotBlank(message = "Package name is required")
    @Size(max = 100, message = "Package name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Destination is required")
    @Size(max = 100, message = "Destination cannot exceed 100 characters")
    private String destination;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public TravelPackage() {
    }

    public TravelPackage(
            String name,
            String destination,
            String description
    ) {
        this.name = name;
        this.destination = destination;
        this.description = description;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}