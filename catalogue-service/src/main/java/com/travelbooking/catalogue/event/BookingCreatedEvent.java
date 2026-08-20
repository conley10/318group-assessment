package com.travelbooking.catalogue.event;

import java.util.UUID;

public record BookingCreatedEvent(
        UUID bookingId,
        UUID customerId,
        Long packageId,
        Long departureId,
        int quantity,
        double totalPrice
) {
}