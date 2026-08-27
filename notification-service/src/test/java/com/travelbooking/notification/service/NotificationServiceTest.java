package com.travelbooking.notification.service;

import com.travelbooking.notification.event.BookingConfirmedEvent;
import com.travelbooking.notification.event.BookingFailedEvent;
import com.travelbooking.notification.model.Notification;
import com.travelbooking.notification.model.NotificationStatus;
import com.travelbooking.notification.model.NotificationType;
import com.travelbooking.notification.repository.NotificationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(repository);
    }

    @Test
    void shouldGetAllNotifications() {
        Notification notification = new Notification();

        when(repository.findAll())
                .thenReturn(List.of(notification));

        List<Notification> result =
                service.getAllNotifications();

        assertEquals(1, result.size());

        verify(repository).findAll();
    }

    @Test
    void shouldGetNotificationById() {
        UUID notificationId = UUID.randomUUID();

        Notification notification = new Notification();
        notification.setNotificationId(notificationId);

        when(repository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        Optional<Notification> result =
                service.getNotificationById(notificationId);

        assertTrue(result.isPresent());
        assertEquals(
                notificationId,
                result.get().getNotificationId()
        );

        verify(repository).findById(notificationId);
    }

    @Test
    void shouldGetNotificationsByCustomer() {
        UUID customerId = UUID.randomUUID();

        Notification notification = new Notification();
        notification.setCustomerId(customerId);

        when(repository.findByCustomerId(customerId))
                .thenReturn(List.of(notification));

        List<Notification> result =
                service.getNotificationsByCustomer(customerId);

        assertEquals(1, result.size());
        assertEquals(
                customerId,
                result.get(0).getCustomerId()
        );

        verify(repository)
                .findByCustomerId(customerId);
    }

    @Test
    void shouldGetNotificationsByBooking() {
        UUID bookingId = UUID.randomUUID();

        Notification notification = new Notification();
        notification.setBookingId(bookingId);

        when(repository.findByBookingId(bookingId))
                .thenReturn(List.of(notification));

        List<Notification> result =
                service.getNotificationsByBooking(bookingId);

        assertEquals(1, result.size());
        assertEquals(
                bookingId,
                result.get(0).getBookingId()
        );

        verify(repository)
                .findByBookingId(bookingId);
    }

    @Test
    void shouldCreateBookingConfirmedNotification() {
        UUID bookingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        BookingConfirmedEvent event =
                new BookingConfirmedEvent(
                        bookingId,
                        customerId,
                        reservationId
                );

        when(repository.save(any(Notification.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Notification result =
                service.createBookingConfirmedNotification(event);

        assertEquals(
                bookingId,
                result.getBookingId()
        );

        assertEquals(
                customerId,
                result.getCustomerId()
        );

        assertEquals(
                NotificationType.BOOKING_CONFIRMED,
                result.getType()
        );

        assertEquals(
                "Your booking has been confirmed.",
                result.getMessage()
        );

        assertEquals(
                NotificationStatus.SENT,
                result.getStatus()
        );

        verify(repository)
                .save(any(Notification.class));
    }

    @Test
    void shouldCreateBookingFailedNotification() {
        UUID bookingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();

        BookingFailedEvent event =
                new BookingFailedEvent(
                        bookingId,
                        customerId,
                        reservationId,
                        "Payment failed"
                );

        when(repository.save(any(Notification.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Notification result =
                service.createBookingFailedNotification(event);

        assertEquals(
                bookingId,
                result.getBookingId()
        );

        assertEquals(
                customerId,
                result.getCustomerId()
        );

        assertEquals(
                NotificationType.BOOKING_FAILED,
                result.getType()
        );

        assertEquals(
                "Your booking could not be completed: Payment failed",
                result.getMessage()
        );

        assertEquals(
                NotificationStatus.SENT,
                result.getStatus()
        );

        verify(repository)
                .save(any(Notification.class));
    }
}