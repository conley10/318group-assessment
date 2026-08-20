package com.travelbooking.catalogue.service;

import com.travelbooking.catalogue.model.DepartureStatus;
import com.travelbooking.catalogue.model.InventoryReservation;
import com.travelbooking.catalogue.model.PackageDeparture;
import com.travelbooking.catalogue.model.ReservationStatus;
import com.travelbooking.catalogue.repository.InventoryReservationRepository;
import com.travelbooking.catalogue.repository.PackageDepartureRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock
    private InventoryReservationRepository reservationRepository;

    @Mock
    private PackageDepartureRepository departureRepository;

    private InventoryReservationService service;

    private PackageDeparture departure;

    @BeforeEach
    void setUp() {

        service = new InventoryReservationService(
                reservationRepository,
                departureRepository
        );

        departure = new PackageDeparture();
        departure.setDepartureId(1L);
        departure.setCapacity(20);
        departure.setStatus(DepartureStatus.AVAILABLE);
    }

    @Test
    void shouldCalculateAvailableCapacity() {

        InventoryReservation reservation1 =
                new InventoryReservation();
        reservation1.setQuantity(4);
        reservation1.setStatus(ReservationStatus.RESERVED);

        InventoryReservation reservation2 =
                new InventoryReservation();
        reservation2.setQuantity(3);
        reservation2.setStatus(ReservationStatus.CONFIRMED);

        when(departureRepository.findById(1L))
                .thenReturn(Optional.of(departure));

        when(reservationRepository
                .findByDepartureDepartureIdAndStatusIn(
                        eq(1L),
                        anyList()
                ))
                .thenReturn(List.of(
                        reservation1,
                        reservation2
                ));

        int available = service.getAvailableCapacity(1L);

        assertEquals(13, available);
    }

    @Test
    void shouldReserveInventorySuccessfully() {

        UUID bookingId = UUID.randomUUID();

        InventoryReservation request =
                new InventoryReservation();

        request.setBookingId(bookingId);
        request.setQuantity(2);

        when(departureRepository.findById(1L))
                .thenReturn(Optional.of(departure));

        when(reservationRepository.findByBookingId(bookingId))
                .thenReturn(Optional.empty());

        when(reservationRepository
                .findByDepartureDepartureIdAndStatusIn(
                        eq(1L),
                        anyList()
                ))
                .thenReturn(List.of());

        when(reservationRepository.save(any()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ReservationResult result =
                service.reserve(1L, request);

        assertEquals(
                ReservationOutcome.CREATED,
                result.outcome()
        );

        assertNotNull(result.reservation());

        assertEquals(
                ReservationStatus.RESERVED,
                result.reservation().getStatus()
        );

        assertEquals(
                departure,
                result.reservation().getDeparture()
        );

        verify(reservationRepository, times(1))
                .save(request);
    }

    @Test
    void shouldRejectReservationWhenCapacityIsInsufficient() {

        UUID bookingId = UUID.randomUUID();

        InventoryReservation request =
                new InventoryReservation();

        request.setBookingId(bookingId);
        request.setQuantity(5);

        InventoryReservation existing =
                new InventoryReservation();

        existing.setQuantity(18);
        existing.setStatus(
                ReservationStatus.CONFIRMED
        );

        when(departureRepository.findById(1L))
                .thenReturn(Optional.of(departure));

        when(reservationRepository.findByBookingId(bookingId))
                .thenReturn(Optional.empty());

        when(reservationRepository
                .findByDepartureDepartureIdAndStatusIn(
                        eq(1L),
                        anyList()
                ))
                .thenReturn(List.of(existing));

        ReservationResult result =
                service.reserve(1L, request);

        assertEquals(
                ReservationOutcome.INSUFFICIENT_CAPACITY,
                result.outcome()
        );

        assertNull(result.reservation());

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectDuplicateBooking() {

        UUID bookingId = UUID.randomUUID();

        InventoryReservation request =
                new InventoryReservation();

        request.setBookingId(bookingId);
        request.setQuantity(2);

        InventoryReservation existing =
                new InventoryReservation();

        existing.setBookingId(bookingId);
        existing.setQuantity(2);

        when(departureRepository.findById(1L))
                .thenReturn(Optional.of(departure));

        when(reservationRepository.findByBookingId(bookingId))
                .thenReturn(Optional.of(existing));

        ReservationResult result =
                service.reserve(1L, request);

        assertEquals(
                ReservationOutcome.DUPLICATE_BOOKING,
                result.outcome()
        );

        assertNull(result.reservation());

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectReservationWhenDepartureDoesNotExist() {

        InventoryReservation request =
                new InventoryReservation();

        request.setBookingId(UUID.randomUUID());
        request.setQuantity(2);

        when(departureRepository.findById(999L))
                .thenReturn(Optional.empty());

        ReservationResult result =
                service.reserve(999L, request);

        assertEquals(
                ReservationOutcome.DEPARTURE_NOT_FOUND,
                result.outcome()
        );

        assertNull(result.reservation());

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectReservationWhenDepartureUnavailable() {

        departure.setStatus(
                DepartureStatus.CANCELLED
        );

        InventoryReservation request =
                new InventoryReservation();

        request.setBookingId(UUID.randomUUID());
        request.setQuantity(2);

        when(departureRepository.findById(1L))
                .thenReturn(Optional.of(departure));

        ReservationResult result =
                service.reserve(1L, request);

        assertEquals(
                ReservationOutcome.DEPARTURE_UNAVAILABLE,
                result.outcome()
        );

        assertNull(result.reservation());

        verify(reservationRepository, never())
                .save(any());
    }

    @Test
    void shouldConfirmReservation() {

        UUID reservationId = UUID.randomUUID();

        InventoryReservation reservation =
                new InventoryReservation();

        reservation.setReservationId(reservationId);
        reservation.setStatus(
                ReservationStatus.RESERVED
        );

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        Optional<InventoryReservation> result =
                service.confirmReservation(reservationId);

        assertTrue(result.isPresent());

        assertEquals(
                ReservationStatus.CONFIRMED,
                result.get().getStatus()
        );

        verify(reservationRepository)
                .save(reservation);
    }

    @Test
    void shouldReleaseReservation() {

        UUID reservationId = UUID.randomUUID();

        InventoryReservation reservation =
                new InventoryReservation();

        reservation.setReservationId(reservationId);
        reservation.setStatus(
                ReservationStatus.RESERVED
        );

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        Optional<InventoryReservation> result =
                service.releaseReservation(reservationId);

        assertTrue(result.isPresent());

        assertEquals(
                ReservationStatus.RELEASED,
                result.get().getStatus()
        );

        verify(reservationRepository)
                .save(reservation);
    }
}