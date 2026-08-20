package com.travelbooking.booking.event;

import java.util.UUID;

public record InventoryRejectedEvent(
        UUID bookingId,
        Long departureId,
        String reason
) {
}