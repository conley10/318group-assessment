package com.travelbooking.notification.controller;

import com.travelbooking.notification.model.Notification;
import com.travelbooking.notification.model.NotificationStatus;
import com.travelbooking.notification.model.NotificationType;
import com.travelbooking.notification.service.NotificationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void shouldGetAllNotifications() throws Exception {

        Notification notification = createNotification();

        when(notificationService.getAllNotifications())
                .thenReturn(List.of(notification));

        mockMvc.perform(
                        get("/api/notifications")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId")
                        .value(notification.getNotificationId().toString()))
                .andExpect(jsonPath("$[0].bookingId")
                        .value(notification.getBookingId().toString()))
                .andExpect(jsonPath("$[0].customerId")
                        .value(notification.getCustomerId().toString()))
                .andExpect(jsonPath("$[0].type")
                        .value("BOOKING_CONFIRMED"))
                .andExpect(jsonPath("$[0].status")
                        .value("SENT"))
                .andExpect(jsonPath("$[0].message")
                        .value("Your booking has been confirmed."));
    }

    @Test
    void shouldGetNotificationById() throws Exception {

        Notification notification = createNotification();

        when(notificationService.getNotificationById(
                notification.getNotificationId()
        )).thenReturn(Optional.of(notification));

        mockMvc.perform(
                        get(
                                "/api/notifications/{notificationId}",
                                notification.getNotificationId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId")
                        .value(notification.getNotificationId().toString()))
                .andExpect(jsonPath("$.type")
                        .value("BOOKING_CONFIRMED"))
                .andExpect(jsonPath("$.status")
                        .value("SENT"));
    }

    @Test
    void shouldReturn404WhenNotificationDoesNotExist()
            throws Exception {

        UUID notificationId = UUID.randomUUID();

        when(notificationService.getNotificationById(notificationId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get(
                                "/api/notifications/{notificationId}",
                                notificationId
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetNotificationsByCustomer()
            throws Exception {

        Notification notification = createNotification();

        when(notificationService.getNotificationsByCustomer(
                notification.getCustomerId()
        )).thenReturn(List.of(notification));

        mockMvc.perform(
                        get(
                                "/api/notifications/customer/{customerId}",
                                notification.getCustomerId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId")
                        .value(notification.getCustomerId().toString()))
                .andExpect(jsonPath("$[0].type")
                        .value("BOOKING_CONFIRMED"));
    }

    @Test
    void shouldGetNotificationsByBooking()
            throws Exception {

        Notification notification = createNotification();

        when(notificationService.getNotificationsByBooking(
                notification.getBookingId()
        )).thenReturn(List.of(notification));

        mockMvc.perform(
                        get(
                                "/api/notifications/booking/{bookingId}",
                                notification.getBookingId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId")
                        .value(notification.getBookingId().toString()))
                .andExpect(jsonPath("$[0].status")
                        .value("SENT"));
    }

    @Test
    void shouldReturnEmptyListWhenCustomerHasNoNotifications()
            throws Exception {

        UUID customerId = UUID.randomUUID();

        when(notificationService.getNotificationsByCustomer(customerId))
                .thenReturn(List.of());

        mockMvc.perform(
                        get(
                                "/api/notifications/customer/{customerId}",
                                customerId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    private Notification createNotification() {

        Notification notification = new Notification();

        notification.setNotificationId(UUID.randomUUID());
        notification.setBookingId(UUID.randomUUID());
        notification.setCustomerId(UUID.randomUUID());

        notification.setType(
                NotificationType.BOOKING_CONFIRMED
        );

        notification.setMessage(
                "Your booking has been confirmed."
        );

        notification.setStatus(
                NotificationStatus.SENT
        );

        return notification;
    }
}