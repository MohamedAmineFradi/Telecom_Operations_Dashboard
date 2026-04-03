package org.example.producer.service;

import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.example.producer.model.HourlyTrafficRecord;
import org.example.producer.repository.HourlyTrafficRecordRepository;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TrafficEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TrafficEventProducer.class);
    private static final String WATERMARK_STREAM = "traffic";
    private static final Duration DEFAULT_SLOT_STEP = Duration.ofHours(1);
    private static final long PAGE_SEND_TIMEOUT_SECONDS = 15L;

    private static final String TOPIC_SMS      = "activity.sms";
    private static final String TOPIC_CALL     = "activity.call";
    private static final String TOPIC_INTERNET = "activity.internet";
    private static final String TOPIC_TRAFFIC  = "activity.traffic";

    private final HourlyTrafficRecordRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WatermarkStore watermarkStore;
    private final OffsetDateTime replayStart;
    private final OffsetDateTime replayEnd;
    private final Duration replayStep;
    private final int replayBatchSize;

    public TrafficEventProducer(HourlyTrafficRecordRepository repository,
                                KafkaTemplate<String, Object> kafkaTemplate,
                                WatermarkStore watermarkStore,
                                @Value("${producer.replay.start:2013-11-01T00:00:00Z}") OffsetDateTime replayStart,
                                @Value("${producer.replay.end:2013-11-07T23:59:59Z}") OffsetDateTime replayEnd,
                                @Value("${producer.replay.step:PT1H}") Duration replayStep,
                                @Value("${producer.replay.batch-size:25}") int replayBatchSize) {
        this.repository      = repository;
        this.kafkaTemplate   = kafkaTemplate;
        this.watermarkStore  = watermarkStore;
        this.replayStart     = replayStart;
        this.replayEnd       = replayEnd;
        this.replayStep      = replayStep;
        this.replayBatchSize = replayBatchSize;
    }

    // polls every 5 seconds
    @Scheduled(
            fixedDelayString = "${producer.poll.interval-ms:5000}",
            initialDelayString = "${producer.poll.initial-delay-ms:5000}"
    )
    public void poll() {
        OffsetDateTime replayStartUtc        = normalizeToUtcHourBoundary(replayStart);
        OffsetDateTime replayEndExclusiveUtc = toUtcHourExclusiveBoundary(replayEnd);
        OffsetDateTime slotStart = normalizeToUtcHourBoundary(
                watermarkStore.getLast(WATERMARK_STREAM, replayStartUtc)
        );

        if (!slotStart.isBefore(replayEndExclusiveUtc)) {
            log.info("Traffic replay completed at {}", replayEndExclusiveUtc);
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
            List<HourlyTrafficRecord> records = repository
                    .findByHourGreaterThanEqualAndHourLessThanOrderByHourAscCellIdAsc(
                            slotStart, slotEnd, pageable
                    );

            if (records.isEmpty()) {
                break;
            }

                List<CompletableFuture<?>> sendFutures = new ArrayList<>(records.size() * 4);

                try {
                for (HourlyTrafficRecord r : records) {
                    String key = String.valueOf(r.getCellId());

                    sendFutures.add(
                        kafkaTemplate.send(TOPIC_SMS, key,
                            new SmsEvent(r.getHour(), r.getCellId(),
                                r.getTotalSmsin(), r.getTotalSmsout()))
                    );

                    sendFutures.add(
                        kafkaTemplate.send(TOPIC_CALL, key,
                            new CallEvent(r.getHour(), r.getCellId(),
                                r.getTotalCallin(), r.getTotalCallout()))
                    );

                    sendFutures.add(
                        kafkaTemplate.send(TOPIC_INTERNET, key,
                            new InternetEvent(r.getHour(), r.getCellId(),
                                r.getTotalInternet()))
                    );

                    sendFutures.add(
                        kafkaTemplate.send(TOPIC_TRAFFIC, key,
                            new TrafficEvent(r.getHour(), r.getCellId(),
                                r.getTotalSmsin(), r.getTotalSmsout(),
                                r.getTotalCallin(), r.getTotalCallout(),
                                r.getTotalInternet(), r.getTotalActivity()))
                    );
                }

                CompletableFuture
                    .allOf(sendFutures.toArray(CompletableFuture[]::new))
                    .get(PAGE_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Exception exception) {
                log.error("Traffic replay failed for slot [{} -> {})", slotStart, slotEnd, exception);
                return;
            }

            sentCount += records.size();
            page++;
        }

        if (sentCount == 0) {
            log.warn("No traffic records in replay slot [{} -> {}), skipping", slotStart, slotEnd);
            watermarkStore.update(WATERMARK_STREAM, slotEnd);
            return;
        }

        log.info("Sent {} traffic records to Kafka (slot: {} -> {}, batchSize: {}, replayStep: {})",
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