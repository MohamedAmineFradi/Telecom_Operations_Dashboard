package org.telecom_operations_dashboard.call.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.call.service.CallCurrentStateService;
import org.telecom_operations_dashboard.common.dto.event.CallEvent;

@Service
@RequiredArgsConstructor
public class CallIngestionConsumer {

    private final CallCurrentStateService callCurrentStateService;
    private final CallRawSseBroadcaster callRawSseBroadcaster;

    @KafkaListener(
            topics = "${kafka.topics.call:activity.call}",
            groupId = "${app.kafka.sse.group-id:${spring.application.name:${app.service:service}}-sse-${HOSTNAME:${random.uuid}}}"
    )
    public void onCallEvent(CallEvent event) {
        if (event == null || event.cellId() == null) {
            return;
        }

        callCurrentStateService.upsert(event);
        callRawSseBroadcaster.broadcastRawEvent(event);
    }
}
