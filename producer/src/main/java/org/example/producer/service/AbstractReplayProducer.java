package org.example.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.telecom_operations_dashboard.common.config.SimulationProperties;

/**
 * Abstract base class for replaying historical events from a repository to Kafka.
 *
 * @param <T> The type of the record being replayed.
 */
public abstract class AbstractReplayProducer<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final KafkaTemplate<String, Object> kafkaTemplate;
    protected final WatermarkStore watermarkStore;
    protected final SimulationProperties simulationProperties;

    private static final Duration DEFAULT_SLOT_STEP = Duration.ofHours(1);

    protected AbstractReplayProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            WatermarkStore watermarkStore,
            SimulationProperties simulationProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.watermarkStore = watermarkStore;
        this.simulationProperties = simulationProperties;
    }

    /**
     * The core polling logic that iterates through time slots and pages of records.
     */
    protected void doPoll(OffsetDateTime targetEnd) {
        OffsetDateTime replayStartUtc = normalizeToUtcHourBoundary(simulationProperties.getStart());
        OffsetDateTime slotStart = normalizeToUtcHourBoundary(
                watermarkStore.getLast(getStreamName(), replayStartUtc)
        );

        if (!slotStart.isBefore(targetEnd)) {
            log.debug("{} already caught up to tick {}", getStreamName(), targetEnd);
            return;
        }

        // Process at most one simulation step per tick to avoid producer starvation.
        OffsetDateTime slotEnd = slotStart.plus(getEffectiveReplayStep());
        if (slotEnd.isAfter(targetEnd)) {
            slotEnd = targetEnd;
        }

        long sentCount = 0;
        int page = 0;

        while (true) {
            Pageable pageable = PageRequest.of(page, simulationProperties.getBatchSize());
            List<T> records = fetchRecords(slotStart, slotEnd, pageable);

            if (records == null || records.isEmpty()) {
                break;
            }

            List<CompletableFuture<?>> sendFutures = new ArrayList<>();

            try {
                for (T record : records) {
                    sendFutures.addAll(sendKafkaEvents(record));
                }

                CompletableFuture
                        .allOf(sendFutures.toArray(CompletableFuture[]::new))
                    .get(getPageSendTimeoutSeconds(), TimeUnit.SECONDS);
            } catch (Exception exception) {
                log.error("{} replay failed for slot [{} -> {})", getStreamName(), slotStart, slotEnd, exception);
                return;
            }

            sentCount += records.size();
            page++;
        }

        if (sentCount > 0) {
            log.info("Sent {} {} records to Kafka (slot: {} -> {})",
                    sentCount, getStreamName(), slotStart, slotEnd);
        }

        watermarkStore.update(getStreamName(), slotEnd);
    }

    /**
     * Fetches a page of records from the repository for a given time slot.
     */
    protected abstract List<T> fetchRecords(OffsetDateTime start, OffsetDateTime end, Pageable pageable);

    /**
     * Sends the necessary Kafka events for a given record.
     * Returns a list of futures to track completeness.
     */
    protected abstract List<CompletableFuture<?>> sendKafkaEvents(T record);

    /**
     * The unique name of the stream for watermarking.
     */
    protected abstract String getStreamName();

    protected Duration getEffectiveReplayStep() {
        Duration step = simulationProperties.getStep();
        if (step == null || step.isZero() || step.isNegative()) {
            return DEFAULT_SLOT_STEP;
        }
        return step;
    }

    protected long getPageSendTimeoutSeconds() {
        int timeoutSeconds = simulationProperties.getPageSendTimeoutSeconds();
        if (timeoutSeconds <= 0) {
            return 15L;
        }
        return timeoutSeconds;
    }

    protected OffsetDateTime normalizeToUtcHourBoundary(OffsetDateTime timestamp) {
        return timestamp.withOffsetSameInstant(ZoneOffset.UTC)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    protected OffsetDateTime toUtcHourExclusiveBoundary(OffsetDateTime timestamp) {
        OffsetDateTime utcTimestamp = timestamp.withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime hourBoundary = normalizeToUtcHourBoundary(utcTimestamp);
        if (utcTimestamp.equals(hourBoundary)) {
            return hourBoundary;
        }
        return hourBoundary.plusHours(1);
    }
}
