package org.example.telecom_operations_dashboard.service.Impl;

import org.example.telecom_operations_dashboard.dto.StreamSlotResultDto;
import org.example.telecom_operations_dashboard.dto.StreamStatusDto;
import org.example.telecom_operations_dashboard.dto.TrafficEvent;
import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.example.telecom_operations_dashboard.repository.HourlyTrafficRepository;
import org.example.telecom_operations_dashboard.service.TrafficStreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TrafficStreamingServiceImpl implements TrafficStreamingService {

    private static final Logger log = LoggerFactory.getLogger(TrafficStreamingServiceImpl.class);

    private final KafkaTemplate<String, TrafficEvent> kafkaTemplate;
    private final HourlyTrafficRepository hourlyTrafficRepository;
    private final AtomicReference<OffsetDateTime> currentStreamSlot;
    private volatile OffsetDateTime lastSentAt;
    private volatile int lastSentCount;
    private final AtomicBoolean streamingEnabled = new AtomicBoolean(false);

    @Value(value = "${traffic.kafka.topic}")
    private String topic;

    public TrafficStreamingServiceImpl(KafkaTemplate<String, TrafficEvent> kafkaTemplate,
                                       HourlyTrafficRepository repo) {
        this.kafkaTemplate = kafkaTemplate;
        this.hourlyTrafficRepository = repo;
        this.currentStreamSlot = new AtomicReference<>(OffsetDateTime.parse("2013-11-01T18:00:00Z"));
        this.lastSentAt = null;
        this.lastSentCount = 0;
    }

    @Override
    public StreamSlotResultDto streamSlot(OffsetDateTime slotDatetime) {
        OffsetDateTime hour = slotDatetime != null ? slotDatetime : currentStreamSlot.get();
        var records = hourlyTrafficRepository.findTopCellsAtHour(hour, 50);

        int recordCount = 0;
        for (HourlyTrafficView view : records) {
            TrafficEvent event = new TrafficEvent();
            event.setDatetime(hour);
            event.setCellId(view.getCellId());
            event.setTotalActivity(view.getTotalActivity());
            event.setTotalSmsin(view.getTotalSmsin());
            event.setTotalSmsout(view.getTotalSmsout());
            event.setTotalCallin(view.getTotalCallin());
            event.setTotalCallout(view.getTotalCallout());
            event.setTotalInternet(view.getTotalInternet());

            kafkaTemplate.send(topic, view.getCellId().toString(), event);
            recordCount++;
        }

        lastSentAt = OffsetDateTime.now();
        lastSentCount = recordCount;
        log.info("Streamed {} events for slot {}", recordCount, hour);
        
        if (slotDatetime == null) {
            currentStreamSlot.updateAndGet(slot -> slot.plusHours(1));
        }
        
        return new StreamSlotResultDto(hour.toString(), recordCount);
    }

    @Override
    public StreamStatusDto getStatus() {
        return new StreamStatusDto(
                currentStreamSlot.get().toString(),
                lastSentAt != null ? lastSentAt.toString() : null,
                lastSentCount
        );
    }

    @Scheduled(fixedDelay = 10000)
    public void streamNextSlot() {
        if (!streamingEnabled.get()) {
            log.debug("Scheduled streaming disabled, skipping slot");
            return;
        }
        streamSlot(null);
    }
    
    public void enableStreaming() {
        streamingEnabled.set(true);
        log.info("Streaming enabled");
    }
    
    public void disableStreaming() {
        streamingEnabled.set(false);
        log.info("Streaming disabled");
    }
    
    public boolean isStreamingEnabled() {
        return streamingEnabled.get();
    }
}
