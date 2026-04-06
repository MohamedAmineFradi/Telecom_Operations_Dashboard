package org.telecom_operations_dashboard.alert.consumer;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.telecom_operations_dashboard.alert.service.AlertService;
import org.telecom_operations_dashboard.common.dto.event.CongestionEvent;

@Component
@RequiredArgsConstructor
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);
    private final AlertService alertService;

    @KafkaListener(
            topics = "alerts.congestion",
            groupId = "alert-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCongestionEvent(CongestionEvent event) {
        log.info("Received congestion event for cell {} with severity {}", event.getCellId(), event.getSeverity());
        try {
            alertService.handleCongestionEvent(event);
        } catch (Exception e) {
            log.error("Failed to process congestion event: {}", e.getMessage(), e);
        }
    }
}
