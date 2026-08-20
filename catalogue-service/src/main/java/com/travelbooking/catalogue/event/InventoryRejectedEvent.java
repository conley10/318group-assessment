package com.travelbooking.catalogue.event;

import java.util.UUID;

public record InventoryRejectedEvent(
        UUID bookingId,
        Long departureId,
        String reason
) {
}