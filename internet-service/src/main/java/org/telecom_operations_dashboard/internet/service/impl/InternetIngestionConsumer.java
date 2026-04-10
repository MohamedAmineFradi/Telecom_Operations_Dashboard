package org.telecom_operations_dashboard.internet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.internet.service.InternetCurrentStateService;

@Service
@RequiredArgsConstructor
public class InternetIngestionConsumer {

    private final InternetCurrentStateService internetCurrentStateService;
    private final InternetRawSseBroadcaster internetRawSseBroadcaster;

    @KafkaListener(
            topics = "${kafka.topics.internet:activity.internet}",
            groupId = "${app.kafka.sse.group-id:${spring.application.name:${app.service:service}}-sse-${HOSTNAME:${random.uuid}}}"
    )
    public void onInternetEvent(InternetEvent event) {
        if (event == null || event.cellId() == null) {
            return;
        }

        internetCurrentStateService.upsert(event);
        internetRawSseBroadcaster.broadcastRawEvent(event);
    }
}
