
package com.travelbooking.stream_analytics_service.analytics;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class AnalyticsService {

    private final AtomicLong totalBookings = new AtomicLong(0);
    private final AtomicLong confirmedBookings = new AtomicLong(0);
    private final AtomicLong failedBookings = new AtomicLong(0);

    private final ObjectProvider<InteractiveQueryService> queryServiceProvider;

    public AnalyticsService(
            ObjectProvider<InteractiveQueryService> queryServiceProvider) {
        this.queryServiceProvider = queryServiceProvider;
    }

    public void setTotalBookings(long count) {
        totalBookings.set(count);
    }

    public void setConfirmedBookings(long count) {
        confirmedBookings.set(count);
    }

    public void setFailedBookings(long count) {
        failedBookings.set(count);
    }

    private long readCount(String storeName, String key, long fallback) {
        try {
            InteractiveQueryService queryService =
                    queryServiceProvider.getIfAvailable();

            if (queryService == null) {
                return fallback;
            }

            ReadOnlyKeyValueStore<String, Long> store =
                    queryService.getQueryableStore(
                            storeName,
                            QueryableStoreTypes.keyValueStore()
                    );

            Long count = store.get(key);
            return count != null ? count : fallback;

        } catch (Exception e) {
            return fallback;
        }
    }

    public long getTotalBookings() {
        return readCount(
                "booking-created-count-store",
                "total",
                totalBookings.get()
        );
    }

    public long getConfirmedBookings() {
        return readCount(
                "booking-confirmed-count-store",
                "confirmed",
                confirmedBookings.get()
        );
    }

    public long getFailedBookings() {
        return readCount(
                "booking-failed-count-store",
                "failed",
                failedBookings.get()
        );
    }

    public double getSuccessRate() {
        long total = getTotalBookings();

        if (total == 0) {
            return 0.0;
        }

        return ((double) getConfirmedBookings() / total) * 100;
    }
}
