package com.travelbooking.catalogue.service;

import com.travelbooking.catalogue.model.InventoryReservation;

public record ReservationResult(
        ReservationOutcome outcome,
        InventoryReservation reservation
) {
}