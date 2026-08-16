package com.travelbooking.catalogue.service;

import com.travelbooking.catalogue.model.DepartureStatus;
import com.travelbooking.catalogue.model.InventoryReservation;
import com.travelbooking.catalogue.model.PackageDeparture;
import com.travelbooking.catalogue.model.ReservationStatus;
import com.travelbooking.catalogue.repository.InventoryReservationRepository;
import com.travelbooking.catalogue.repository.PackageDepartureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(
                    ReservationStatus.RESERVED,
                    ReservationStatus.CONFIRMED
            );

    private final InventoryReservationRepository reservationRepository;
    private final PackageDepartureRepository departureRepository;

    public InventoryReservationService(
            InventoryReservationRepository reservationRepository,
            PackageDepartureRepository departureRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.departureRepository = departureRepository;
    }

    public List<InventoryReservation> getReservationsForDeparture(
            Long departureId
    ) {
        return reservationRepository
                .findByDepartureDepartureId(departureId);
    }

    public Optional<InventoryReservation> getReservation(
            UUID reservationId
    ) {
        return reservationRepository.findById(reservationId);
    }

    public int getAvailableCapacity(Long departureId) {
        PackageDeparture departure = departureRepository
                .findById(departureId)
                .orElseThrow();

        int reservedQuantity = reservationRepository
                .findByDepartureDepartureIdAndStatusIn(
                        departureId,
                        ACTIVE_STATUSES
                )
                .stream()
                .mapToInt(InventoryReservation::getQuantity)
                .sum();

        return departure.getCapacity() - reservedQuantity;
    }

    @Transactional
    public ReservationResult reserve(
            Long departureId,
            InventoryReservation request
    ) {
        Optional<PackageDeparture> departureOptional =
                departureRepository.findById(departureId);

        if (departureOptional.isEmpty()) {
            return new ReservationResult(
                    ReservationOutcome.DEPARTURE_NOT_FOUND,
                    null
            );
        }

        PackageDeparture departure = departureOptional.get();

        if (departure.getStatus() != DepartureStatus.AVAILABLE) {
            return new ReservationResult(
                    ReservationOutcome.DEPARTURE_UNAVAILABLE,
                    null
            );
        }

        if (reservationRepository
                .findByBookingId(request.getBookingId())
                .isPresent()) {

            return new ReservationResult(
                    ReservationOutcome.DUPLICATE_BOOKING,
                    null
            );
        }

        int availableCapacity = getAvailableCapacity(departureId);

        if (request.getQuantity() > availableCapacity) {
            return new ReservationResult(
                    ReservationOutcome.INSUFFICIENT_CAPACITY,
                    null
            );
        }

        request.setReservationId(null);
        request.setDeparture(departure);
        request.setStatus(ReservationStatus.RESERVED);

        InventoryReservation saved =
                reservationRepository.save(request);

        return new ReservationResult(
                ReservationOutcome.CREATED,
                saved
        );
    }

    @Transactional
    public Optional<InventoryReservation> confirmReservation(
            UUID reservationId
    ) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    reservation.setStatus(
                            ReservationStatus.CONFIRMED
                    );

                    return reservationRepository.save(reservation);
                });
    }

    @Transactional
    public Optional<InventoryReservation> releaseReservation(
            UUID reservationId
    ) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    reservation.setStatus(
                            ReservationStatus.RELEASED
                    );

                    return reservationRepository.save(reservation);
                });
    }
}