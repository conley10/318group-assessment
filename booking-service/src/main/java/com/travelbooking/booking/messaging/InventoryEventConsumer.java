package com.travelbooking.booking.messaging;

import com.travelbooking.booking.event.InventoryRejectedEvent;
import com.travelbooking.booking.event.InventoryReservedEvent;
import com.travelbooking.booking.service.BookingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class InventoryEventConsumer {

    private final BookingService bookingService;

    public InventoryEventConsumer(
            BookingService bookingService
    ) {
        this.bookingService = bookingService;
    }

    @Bean
    public Consumer<InventoryReservedEvent> inventoryReserved() {
        return event -> {

            System.out.println(
                    "Booking Service received InventoryReserved: "
                            + event.bookingId()
            );

            bookingService.handleInventoryReserved(event);
        };
    }

    @Bean
    public Consumer<InventoryRejectedEvent> inventoryRejected() {
        return event -> {

            System.out.println(
                    "Booking Service received InventoryRejected: "
                            + event.bookingId()
                            + " reason: "
                            + event.reason()
            );

            bookingService.handleInventoryRejected(event);
        };
    }
}