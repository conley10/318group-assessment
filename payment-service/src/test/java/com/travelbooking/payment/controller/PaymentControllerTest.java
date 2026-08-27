package com.travelbooking.payment.controller;

import com.travelbooking.payment.model.Payment;
import com.travelbooking.payment.model.PaymentStatus;
import com.travelbooking.payment.service.PaymentService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void shouldGetAllPayments() throws Exception {

        Payment payment = createPayment();

        when(paymentService.getAllPayments())
                .thenReturn(List.of(payment));

        mockMvc.perform(
                        get("/api/payments")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId")
                        .value(payment.getPaymentId().toString()))
                .andExpect(jsonPath("$[0].bookingId")
                        .value(payment.getBookingId().toString()))
                .andExpect(jsonPath("$[0].customerId")
                        .value(payment.getCustomerId().toString()))
                .andExpect(jsonPath("$[0].amount")
                        .value(4399.98))
                .andExpect(jsonPath("$[0].status")
                        .value("COMPLETED"));
    }

    @Test
    void shouldGetPaymentById() throws Exception {

        Payment payment = createPayment();

        when(paymentService.getPaymentById(
                payment.getPaymentId()
        )).thenReturn(Optional.of(payment));

        mockMvc.perform(
                        get(
                                "/api/payments/{paymentId}",
                                payment.getPaymentId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId")
                        .value(payment.getPaymentId().toString()))
                .andExpect(jsonPath("$.status")
                        .value("COMPLETED"));
    }

    @Test
    void shouldReturn404WhenPaymentDoesNotExist()
            throws Exception {

        UUID paymentId = UUID.randomUUID();

        when(paymentService.getPaymentById(paymentId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get(
                                "/api/payments/{paymentId}",
                                paymentId
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPaymentByBookingId()
            throws Exception {

        Payment payment = createPayment();

        when(paymentService.getPaymentByBookingId(
                payment.getBookingId()
        )).thenReturn(Optional.of(payment));

        mockMvc.perform(
                        get(
                                "/api/payments/booking/{bookingId}",
                                payment.getBookingId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId")
                        .value(payment.getBookingId().toString()))
                .andExpect(jsonPath("$.paymentId")
                        .value(payment.getPaymentId().toString()))
                .andExpect(jsonPath("$.status")
                        .value("COMPLETED"));
    }

    @Test
    void shouldReturn404WhenBookingHasNoPayment()
            throws Exception {

        UUID bookingId = UUID.randomUUID();

        when(paymentService.getPaymentByBookingId(bookingId))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        get(
                                "/api/payments/booking/{bookingId}",
                                bookingId
                        )
                )
                .andExpect(status().isNotFound());
    }

    private Payment createPayment() {

        Payment payment = new Payment();

        payment.setPaymentId(UUID.randomUUID());
        payment.setBookingId(UUID.randomUUID());
        payment.setCustomerId(UUID.randomUUID());

        payment.setAmount(4399.98);

        payment.setStatus(
                PaymentStatus.COMPLETED
        );

        payment.setProcessedAt(
                LocalDateTime.now()
        );

        return payment;
    }
}