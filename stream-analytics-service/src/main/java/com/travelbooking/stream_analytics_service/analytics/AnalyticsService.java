package com.travelbooking.stream_analytics_service.analytics;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class AnalyticsService {

    private final AtomicLong totalBookings = new AtomicLong(0);
    private final AtomicLong confirmedBookings = new AtomicLong(0);
    private final AtomicLong failedBookings = new AtomicLong(0);

    public void bookingCreated() {
        totalBookings.incrementAndGet();
    }

    public void bookingConfirmed() {
        confirmedBookings.incrementAndGet();
    }

    public void bookingFailed() {
        failedBookings.incrementAndGet();
    }

    public long getTotalBookings() {
        return totalBookings.get();
    }

    public long getConfirmedBookings() {
        return confirmedBookings.get();
    }

    public long getFailedBookings() {
        return failedBookings.get();
    }

    public double getSuccessRate() {
        long total = totalBookings.get();

        if (total == 0) {
            return 0.0;
        }

        return ((double) confirmedBookings.get() / total) * 100;
    }
}