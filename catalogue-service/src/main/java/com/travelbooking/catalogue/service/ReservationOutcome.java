package com.travelbooking.catalogue.service;

public enum ReservationOutcome {
    CREATED,
    DEPARTURE_NOT_FOUND,
    DEPARTURE_UNAVAILABLE,
    INSUFFICIENT_CAPACITY,
    DUPLICATE_BOOKING
}