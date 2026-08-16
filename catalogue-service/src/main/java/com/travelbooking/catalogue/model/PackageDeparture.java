package com.travelbooking.catalogue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

@Entity
@Table(name = "package_departures")
public class PackageDeparture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departureId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @DecimalMin(
            value = "0.01",
            message = "Price must be greater than zero"
    )
    private double price;

    @Min(
            value = 1,
            message = "Capacity must be at least 1"
    )
    private int capacity;

    @Enumerated(EnumType.STRING)
    private DepartureStatus status = DepartureStatus.AVAILABLE;

@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "package_id", nullable = false)
private TravelPackage travelPackage;

    public PackageDeparture() {
    }

    public Long getDepartureId() {
        return departureId;
    }

    public void setDepartureId(Long departureId) {
        this.departureId = departureId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public DepartureStatus getStatus() {
        return status;
    }

    public void setStatus(DepartureStatus status) {
        this.status = status;
    }

    public TravelPackage getTravelPackage() {
        return travelPackage;
    }

    public void setTravelPackage(TravelPackage travelPackage) {
        this.travelPackage = travelPackage;
    }
}