package com.travelbooking.notification.messaging;

import com.travelbooking.notification.event.BookingConfirmedEvent;
import com.travelbooking.notification.event.BookingFailedEvent;
import com.travelbooking.notification.service.NotificationService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class BookingOutcomeConsumer {

    private final NotificationService notificationService;

    public BookingOutcomeConsumer(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @Bean
    public Consumer<BookingConfirmedEvent> bookingConfirmed() {
        return event -> {

            System.out.println(
                    "Notification Service received BookingConfirmed: "
                            + event.bookingId()
            );

            notificationService
                    .createBookingConfirmedNotification(event);
        };
    }

    @Bean
    public Consumer<BookingFailedEvent> bookingFailed() {
        return event -> {

            System.out.println(
                    "Notification Service received BookingFailed: "
                            + event.bookingId()
            );

            notificationService
                    .createBookingFailedNotification(event);
        };
    }
}