package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.DTO.TrafficEvent;
import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.example.telecom_operations_dashboard.repository.HourlyTrafficRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class TrafficStreamingService {

    private final KafkaTemplate<String, TrafficEvent> kafkaTemplate;
    private final HourlyTrafficRepository hourlyTrafficRepository;

    @Value(value = "${traffic.kafka.topic}")
    private String topic;

    public TrafficStreamingService(KafkaTemplate<String, TrafficEvent> kafkaTemplate,
                                   HourlyTrafficRepository repo) {
        this.kafkaTemplate = kafkaTemplate;
        this.hourlyTrafficRepository = repo;
    }

    @Scheduled(fixedDelay = 10000)
    public void streamNextSlot() {
        OffsetDateTime hour = OffsetDateTime.parse("2013-11-01T18:00:00Z");
        var records = hourlyTrafficRepository.findTopCellsAtHour(hour, 50);

        for (HourlyTrafficView view : records) {
            TrafficEvent event = new TrafficEvent();
            event.setDatetime(hour);
            event.setCellId(view.getCellId());
            event.setTotalActivity(view.getTotalActivity());

            kafkaTemplate.send(topic, view.getCellId().toString(), event);
        }
    }
}
