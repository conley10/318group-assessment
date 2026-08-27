package com.travelbooking.notification.event;

import java.util.UUID;

public record BookingConfirmedEvent(
        UUID bookingId,
        UUID customerId,
        UUID reservationId
) {
}