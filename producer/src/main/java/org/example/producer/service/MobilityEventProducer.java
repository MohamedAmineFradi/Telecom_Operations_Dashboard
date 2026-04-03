package org.example.producer.service;

import org.telecom_operations_dashboard.common.dto.event.MobilityEvent;
import org.example.producer.model.MobilityRecord;
import org.example.producer.repository.MobilityRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

@Service
public class MobilityEventProducer {

    private static final Logger log = LoggerFactory.getLogger(MobilityEventProducer.class);
    private static final String TOPIC_MOBILITY = "activity.mobility";
    private static final String WATERMARK_STREAM = "mobility";
    private static final Duration DEFAULT_SLOT_STEP = Duration.ofHours(1);
    private static final long PAGE_SEND_TIMEOUT_SECONDS = 15L;

    private final MobilityRecordRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WatermarkStore watermarkStore;
    private final OffsetDateTime replayStart;
    private final OffsetDateTime replayEnd;
    private final Duration replayStep;
    private final int replayBatchSize;

    public MobilityEventProducer(MobilityRecordRepository repository,
                                 KafkaTemplate<String, Object> kafkaTemplate,
                                 WatermarkStore watermarkStore,
                                 @Value("${producer.replay.start:2013-11-01T00:00:00Z}") OffsetDateTime replayStart,
                                 @Value("${producer.replay.end:2013-11-07T23:59:59Z}") OffsetDateTime replayEnd,
                                 @Value("${producer.replay.step:PT5M}") Duration replayStep,
                                 @Value("${producer.replay.batch-size:25}") int replayBatchSize) {
        this.repository    = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.watermarkStore = watermarkStore;
        this.replayStart   = replayStart;
        this.replayEnd     = replayEnd;
        this.replayStep    = replayStep;
        this.replayBatchSize = replayBatchSize;
    }

        @Scheduled(
            fixedDelayString = "${producer.poll.interval-ms:5000}",
            initialDelayString = "${producer.poll.mobility-initial-delay-ms:5000}"
        )
    public void poll() {
        OffsetDateTime replayStartUtc = normalizeToUtcHourBoundary(replayStart);
        OffsetDateTime replayEndExclusiveUtc = toUtcHourExclusiveBoundary(replayEnd);
        OffsetDateTime slotStart = normalizeToUtcHourBoundary(
                watermarkStore.getLast(WATERMARK_STREAM, replayStartUtc)
        );

        if (!slotStart.isBefore(replayEndExclusiveUtc)) {
            log.info("Mobility replay completed at {}", replayEndExclusiveUtc);
            return;
        }

        OffsetDateTime slotEnd = slotStart.plus(getEffectiveReplayStep());
        if (slotEnd.isAfter(replayEndExclusiveUtc)) {
            slotEnd = replayEndExclusiveUtc;
        }

        long sentCount = 0;
        int page = 0;

        while (true) {
            Pageable pageable = PageRequest.of(page, replayBatchSize);
            List<MobilityRecord> records = repository
                    .findByDatetimeGreaterThanEqualAndDatetimeLessThanOrderByDatetimeAscCellIdAscProvinciaAsc(
                            slotStart, slotEnd, pageable
                    );

            if (records.isEmpty()) {
                break;
            }

            List<CompletableFuture<?>> sendFutures = new ArrayList<>(records.size());

            try {
                for (MobilityRecord m : records) {
                    String key = m.getCellId() + ":" + m.getProvincia();
                    sendFutures.add(
                            kafkaTemplate.send(TOPIC_MOBILITY, key,
                                    new MobilityEvent(m.getDatetime(), m.getCellId(),
                                            m.getProvincia(), m.getCell2province(), m.getProvince2cell()))
                    );
                }

                CompletableFuture
                        .allOf(sendFutures.toArray(CompletableFuture[]::new))
                        .get(PAGE_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception exception) {
                log.error("Mobility replay failed for slot [{} -> {})", slotStart, slotEnd, exception);
                return;
            }

            sentCount += records.size();
            page++;
        }

        if (sentCount == 0) {
            log.warn("No mobility records in replay slot [{} -> {}), skipping", slotStart, slotEnd);
            watermarkStore.update(WATERMARK_STREAM, slotEnd);
            return;
        }

        log.info("Sent {} mobility records to Kafka (slot: {} -> {}, batchSize: {}, replayStep: {})",
                sentCount, slotStart, slotEnd, replayBatchSize, getEffectiveReplayStep());

        watermarkStore.update(WATERMARK_STREAM, slotEnd);
    }

    private Duration getEffectiveReplayStep() {
        if (replayStep == null || replayStep.isZero() || replayStep.isNegative()) {
            log.warn("Invalid producer.replay.step [{}]; falling back to {}", replayStep, DEFAULT_SLOT_STEP);
            return DEFAULT_SLOT_STEP;
        }
        return replayStep;
    }

    private OffsetDateTime normalizeToUtcHourBoundary(OffsetDateTime timestamp) {
        return timestamp.withOffsetSameInstant(ZoneOffset.UTC)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private OffsetDateTime toUtcHourExclusiveBoundary(OffsetDateTime timestamp) {
        OffsetDateTime utcTimestamp = timestamp.withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime hourBoundary = normalizeToUtcHourBoundary(utcTimestamp);
        if (utcTimestamp.equals(hourBoundary)) {
            return hourBoundary;
        }
        return hourBoundary.plusHours(1);
    }
}