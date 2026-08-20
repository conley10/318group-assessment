package com.travelbooking.booking.event;

import java.util.UUID;

public record InventoryReservedEvent(
        UUID bookingId,
        UUID reservationId,
        Long departureId,
        int quantity
) {
}