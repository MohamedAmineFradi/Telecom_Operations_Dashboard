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

@Service
public class TrafficStreamingServiceImpl implements TrafficStreamingService {

    private static final Logger log = LoggerFactory.getLogger(TrafficStreamingServiceImpl.class);

    private final KafkaTemplate<String, TrafficEvent> kafkaTemplate;
    private final HourlyTrafficRepository hourlyTrafficRepository;
    private OffsetDateTime currentStreamSlot;
    private OffsetDateTime lastSentAt;
    private int lastSentCount;

    @Value(value = "${traffic.kafka.topic}")
    private String topic;

    public TrafficStreamingServiceImpl(KafkaTemplate<String, TrafficEvent> kafkaTemplate,
                                       HourlyTrafficRepository repo) {
        this.kafkaTemplate = kafkaTemplate;
        this.hourlyTrafficRepository = repo;
        this.currentStreamSlot = OffsetDateTime.parse("2013-11-01T18:00:00Z");
        this.lastSentAt = null;
        this.lastSentCount = 0;
    }

    @Override
    public StreamSlotResultDto streamSlot(OffsetDateTime slotDatetime) {
        OffsetDateTime hour = slotDatetime != null ? slotDatetime : currentStreamSlot;
        var records = hourlyTrafficRepository.findTopCellsAtHour(hour, 50);

        int recordCount = 0;
        for (HourlyTrafficView view : records) {
            TrafficEvent event = new TrafficEvent();
            event.setDatetime(hour);
            event.setCellId(view.getCellId());
            event.setTotalActivity(view.getTotalActivity());

            kafkaTemplate.send(topic, view.getCellId().toString(), event);
            recordCount++;
        }

        lastSentAt = OffsetDateTime.now();
        lastSentCount = recordCount;
        log.info("Streamed {} events for slot {}", recordCount, hour);
        
        if (slotDatetime == null) {
            currentStreamSlot = currentStreamSlot.plusHours(1);
        }
        
        return new StreamSlotResultDto(hour.toString(), recordCount);
    }

    @Override
    public StreamStatusDto getStatus() {
        return new StreamStatusDto(
                currentStreamSlot.toString(),
                lastSentAt != null ? lastSentAt.toString() : null,
                lastSentCount
        );
    }

    @Scheduled(fixedDelay = 10000)
    public void streamNextSlot() {
        streamSlot(null);
    }
}
