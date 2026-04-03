package org.telecom_operations_dashboard.traffic.streaming.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.service", havingValue = "traffic")
@ConditionalOnProperty(prefix = "app.streaming", name = "enabled", havingValue = "true")
public class TrafficIngestionConsumer {

    private final TrafficRawSseBroadcaster trafficRawSseBroadcaster;

    @KafkaListener(
            id = "traffic-ingestion-listener",
            topics = "${kafka.topics.traffic:activity.traffic}",
            groupId = "${app.kafka.raw.group-id:traffic-raw-group}"
    )
    public void onTrafficEvent(TrafficEvent event) {
        if (event == null) {
            return;
        }

        trafficRawSseBroadcaster.broadcastRawEvent(event);
    }
}
