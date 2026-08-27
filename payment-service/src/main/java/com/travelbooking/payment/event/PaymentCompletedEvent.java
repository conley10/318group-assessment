package com.travelbooking.payment.event;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID bookingId,
        UUID paymentId
) {
}