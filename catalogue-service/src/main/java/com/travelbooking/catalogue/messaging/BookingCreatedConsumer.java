package com.travelbooking.catalogue.messaging;

import com.travelbooking.catalogue.event.BookingCreatedEvent;
import com.travelbooking.catalogue.event.InventoryRejectedEvent;
import com.travelbooking.catalogue.event.InventoryReservedEvent;
import com.travelbooking.catalogue.model.InventoryReservation;
import com.travelbooking.catalogue.service.InventoryReservationService;
import com.travelbooking.catalogue.service.ReservationOutcome;
import com.travelbooking.catalogue.service.ReservationResult;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class BookingCreatedConsumer {

    private final InventoryReservationService reservationService;
    private final StreamBridge streamBridge;

    public BookingCreatedConsumer(
            InventoryReservationService reservationService,
            StreamBridge streamBridge
    ) {
        this.reservationService = reservationService;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<BookingCreatedEvent> bookingCreated() {
        return event -> {

            System.out.println(
                    "Catalogue received BookingCreated: "
                            + event.bookingId()
            );

            InventoryReservation request = new InventoryReservation();
            request.setBookingId(event.bookingId());
            request.setQuantity(event.quantity());

            ReservationResult result =
                    reservationService.reserve(
                            event.departureId(),
                            request
                    );

            if (result.outcome() == ReservationOutcome.CREATED) {

                InventoryReservation reservation =
                        result.reservation();

                InventoryReservedEvent reservedEvent =
                        new InventoryReservedEvent(
                                event.bookingId(),
                                reservation.getReservationId(),
                                event.departureId(),
                                event.quantity()
                        );

                streamBridge.send(
                        "inventoryReserved-out-0",
                        reservedEvent
                );

                System.out.println(
                        "Inventory reserved for booking: "
                                + event.bookingId()
                );

            } else {

                InventoryRejectedEvent rejectedEvent =
                        new InventoryRejectedEvent(
                                event.bookingId(),
                                event.departureId(),
                                result.outcome().name()
                        );

                streamBridge.send(
                        "inventoryRejected-out-0",
                        rejectedEvent
                );

                System.out.println(
                        "Inventory rejected for booking: "
                                + event.bookingId()
                                + " reason: "
                                + result.outcome()
                );
            }
        };
    }
}