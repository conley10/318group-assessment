package com.travelbooking.catalogue.repository;

import com.travelbooking.catalogue.model.InventoryReservation;
import com.travelbooking.catalogue.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, UUID> {

    List<InventoryReservation> findByDepartureDepartureId(
            Long departureId
    );

    Optional<InventoryReservation> findByBookingId(
            UUID bookingId
    );

    List<InventoryReservation> findByDepartureDepartureIdAndStatusIn(
            Long departureId,
            List<ReservationStatus> statuses
    );
}