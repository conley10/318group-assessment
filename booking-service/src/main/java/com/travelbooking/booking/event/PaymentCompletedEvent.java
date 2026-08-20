package com.travelbooking.booking.event;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID bookingId,
        UUID paymentId
) {
}