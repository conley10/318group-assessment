package com.travelbooking.catalogue.controller;

import com.travelbooking.catalogue.model.InventoryReservation;
import com.travelbooking.catalogue.service.InventoryReservationService;
import com.travelbooking.catalogue.service.ReservationOutcome;
import com.travelbooking.catalogue.service.ReservationResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class InventoryReservationController {

    private final InventoryReservationService service;

    public InventoryReservationController(
            InventoryReservationService service
    ) {
        this.service = service;
    }

    @GetMapping("/departures/{departureId}/availability")
    public ResponseEntity<Map<String, Integer>> getAvailability(
            @PathVariable Long departureId
    ) {
        try {
            int available = service.getAvailableCapacity(departureId);

            return ResponseEntity.ok(
                    Map.of("availableCapacity", available)
            );
        } catch (Exception exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/departures/{departureId}/reservations")
    public List<InventoryReservation> getReservations(
            @PathVariable Long departureId
    ) {
        return service.getReservationsForDeparture(departureId);
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<InventoryReservation> getReservation(
            @PathVariable UUID reservationId
    ) {
        return service.getReservation(reservationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/departures/{departureId}/reservations")
    public ResponseEntity<?> createReservation(
            @PathVariable Long departureId,
            @Valid @RequestBody InventoryReservation reservation
    ) {
        ReservationResult result =
                service.reserve(departureId, reservation);

        if (result.outcome() == ReservationOutcome.CREATED) {
            InventoryReservation created = result.reservation();

            return ResponseEntity
                    .created(URI.create(
                            "/api/reservations/"
                                    + created.getReservationId()
                    ))
                    .body(created);
        }

        if (result.outcome()
                == ReservationOutcome.DEPARTURE_NOT_FOUND) {

            return ResponseEntity.notFound().build();
        }

        String message = switch (result.outcome()) {
            case DEPARTURE_UNAVAILABLE ->
                    "This departure is not available";

            case INSUFFICIENT_CAPACITY ->
                    "There are not enough available spaces";

            case DUPLICATE_BOOKING ->
                    "A reservation already exists for this booking";

            default ->
                    "The reservation could not be created";
        };

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", message));
    }

    @PutMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<InventoryReservation> confirmReservation(
            @PathVariable UUID reservationId
    ) {
        return service.confirmReservation(reservationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/reservations/{reservationId}/release")
    public ResponseEntity<InventoryReservation> releaseReservation(
            @PathVariable UUID reservationId
    ) {
        return service.releaseReservation(reservationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}