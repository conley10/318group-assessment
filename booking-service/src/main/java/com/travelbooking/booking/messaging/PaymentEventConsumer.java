package com.travelbooking.booking.messaging;

import com.travelbooking.booking.event.PaymentCompletedEvent;
import com.travelbooking.booking.event.PaymentFailedEvent;
import com.travelbooking.booking.service.BookingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class PaymentEventConsumer {

    private final BookingService bookingService;

    public PaymentEventConsumer(
            BookingService bookingService
    ) {
        this.bookingService = bookingService;
    }

    @Bean
    public Consumer<PaymentCompletedEvent> paymentCompleted() {
        return event -> {

            System.out.println(
                    "Booking Service received PaymentCompleted: "
                            + event.bookingId()
            );

            bookingService.handlePaymentCompleted(event);
        };
    }

    @Bean
    public Consumer<PaymentFailedEvent> paymentFailed() {
        return event -> {

            System.out.println(
                    "Booking Service received PaymentFailed: "
                            + event.bookingId()
                            + " reason: "
                            + event.reason()
            );

            bookingService.handlePaymentFailed(event);
        };
    }
}