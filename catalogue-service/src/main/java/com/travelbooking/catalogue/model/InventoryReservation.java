package com.travelbooking.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "inventory_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_booking",
                        columnNames = "booking_id"
                )
        }
)
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reservationId;

    @NotNull(message = "Booking ID is required")
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.RESERVED;

    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_id", nullable = false)
    private PackageDeparture departure;

    public InventoryReservation() {
    }

    @PrePersist
    public void beforeSave() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PackageDeparture getDeparture() {
        return departure;
    }

    public void setDeparture(PackageDeparture departure) {
        this.departure = departure;
    }
}