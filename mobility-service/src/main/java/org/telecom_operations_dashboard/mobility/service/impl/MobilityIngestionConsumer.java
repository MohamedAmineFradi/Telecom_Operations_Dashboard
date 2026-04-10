package org.telecom_operations_dashboard.mobility.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.telecom_operations_dashboard.common.dto.event.MobilityEvent;
import org.telecom_operations_dashboard.mobility.service.MobilityRealtimeQueryService;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.streaming.enabled", havingValue = "true")
public class MobilityIngestionConsumer {

    private final MobilityRealtimeQueryService mobilityRealtimeQueryService;
    private final MobilityRawSseBroadcaster mobilityRawSseBroadcaster;

    @KafkaListener(
            id = "mobility-ingestion-listener",
            topics = "${kafka.topics.mobility:activity.mobility}",
            groupId = "${app.kafka.raw.group-id:mobility-raw-group}"
    )
    public void onMobilityEvent(MobilityEvent event) {
        if (event == null) {
            return;
        }

        mobilityRealtimeQueryService.ingestMobilityEvent(event);
        mobilityRawSseBroadcaster.broadcastRawEvent(event);
    }
}
