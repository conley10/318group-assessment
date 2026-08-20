package com.travelbooking.booking.event;

import java.util.UUID;

public record BookingConfirmedEvent(
        UUID bookingId,
        UUID customerId,
        UUID reservationId
) {
}