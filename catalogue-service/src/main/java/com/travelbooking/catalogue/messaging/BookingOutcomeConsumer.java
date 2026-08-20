package com.travelbooking.catalogue.messaging;

import com.travelbooking.catalogue.event.BookingConfirmedEvent;
import com.travelbooking.catalogue.event.BookingFailedEvent;
import com.travelbooking.catalogue.service.InventoryReservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class BookingOutcomeConsumer {

    private final InventoryReservationService reservationService;

    public BookingOutcomeConsumer(
            InventoryReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    @Bean
    public Consumer<BookingConfirmedEvent> bookingConfirmed() {
        return event -> {

            System.out.println(
                    "Catalogue received BookingConfirmed: "
                            + event.bookingId()
            );

            reservationService.confirmReservation(
                    event.reservationId()
            );
        };
    }

    @Bean
    public Consumer<BookingFailedEvent> bookingFailed() {
        return event -> {

            System.out.println(
                    "Catalogue received BookingFailed: "
                            + event.bookingId()
            );

            reservationService.releaseReservation(
                    event.reservationId()
            );
        };
    }
}