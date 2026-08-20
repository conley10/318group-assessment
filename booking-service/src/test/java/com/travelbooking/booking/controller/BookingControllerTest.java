package com.travelbooking.booking.controller;

import com.travelbooking.booking.model.Booking;
import com.travelbooking.booking.model.BookingStatus;
import com.travelbooking.booking.service.BookingService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private BookingService service;

    @Test
    void shouldGetAllBookings() throws Exception {

        Booking booking = createExampleBooking();

        when(service.getAllBookings())
                .thenReturn(List.of(booking));

        mockMvc.perform(
                        get("/api/bookings")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId")
                        .value(booking.getBookingId().toString()))
                .andExpect(jsonPath("$[0].status")
                        .value("PENDING_INVENTORY"));
    }

    @Test
    void shouldGetBookingById() throws Exception {

        Booking booking = createExampleBooking();

        when(service.getBookingById(
                booking.getBookingId()
        )).thenReturn(Optional.of(booking));

        mockMvc.perform(
                        get(
                                "/api/bookings/{bookingId}",
                                booking.getBookingId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId")
                        .value(booking.getBookingId().toString()))
                .andExpect(jsonPath("$.quantity")
                        .value(2));
    }

    @Test
    void shouldReturn404WhenBookingDoesNotExist()
            throws Exception {

        UUID bookingId = UUID.randomUUID();

        when(service.getBookingById(bookingId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get(
                                "/api/bookings/{bookingId}",
                                bookingId
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateBooking() throws Exception {

        Booking request = new Booking();

        request.setCustomerId(UUID.randomUUID());
        request.setPackageId(1L);
        request.setDepartureId(1L);
        request.setQuantity(2);
        request.setTotalPrice(4399.98);

        Booking created = createExampleBooking();

        when(service.createBooking(any(Booking.class)))
                .thenReturn(created);

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        jsonMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/bookings/"
                                + created.getBookingId()
                ))
                .andExpect(jsonPath("$.bookingId")
                        .value(created.getBookingId().toString()))
                .andExpect(jsonPath("$.status")
                        .value("PENDING_INVENTORY"));
    }

    @Test
    void shouldRejectInvalidBooking() throws Exception {

        Booking invalid = new Booking();

        invalid.setQuantity(0);

        mockMvc.perform(
                        post("/api/bookings")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        jsonMapper.writeValueAsString(
                                                invalid
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteBooking() throws Exception {

        UUID bookingId = UUID.randomUUID();

        when(service.deleteBooking(bookingId))
                .thenReturn(true);

        mockMvc.perform(
                        delete(
                                "/api/bookings/{bookingId}",
                                bookingId
                        )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingUnknownBooking()
            throws Exception {

        UUID bookingId = UUID.randomUUID();

        when(service.deleteBooking(bookingId))
                .thenReturn(false);

        mockMvc.perform(
                        delete(
                                "/api/bookings/{bookingId}",
                                bookingId
                        )
                )
                .andExpect(status().isNotFound());
    }

    private Booking createExampleBooking() {

        Booking booking = new Booking();

        booking.setBookingId(UUID.randomUUID());
        booking.setCustomerId(UUID.randomUUID());
        booking.setPackageId(1L);
        booking.setDepartureId(1L);
        booking.setQuantity(2);
        booking.setTotalPrice(4399.98);
        booking.setStatus(
                BookingStatus.PENDING_INVENTORY
        );

        return booking;
    }
}