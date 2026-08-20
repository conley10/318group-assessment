package com.travelbooking.booking.event;

import java.util.UUID;

public record BookingFailedEvent(
        UUID bookingId,
        UUID customerId,
        UUID reservationId,
        String reason
) {
}