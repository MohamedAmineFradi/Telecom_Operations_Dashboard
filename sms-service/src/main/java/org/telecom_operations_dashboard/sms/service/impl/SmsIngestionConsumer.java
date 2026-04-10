package org.telecom_operations_dashboard.sms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.sms.service.SmsCurrentStateService;

@Service
@RequiredArgsConstructor
public class SmsIngestionConsumer {

    private final SmsCurrentStateService smsCurrentStateService;
    private final SmsRawSseBroadcaster smsRawSseBroadcaster;

    @KafkaListener(
            topics = "${kafka.topics.sms:activity.sms}",
            groupId = "${app.kafka.sse.group-id:${spring.application.name:${app.service:service}}-sse-${HOSTNAME:${random.uuid}}}"
    )
    public void onSmsEvent(SmsEvent event) {
        if (event == null || event.cellId() == null) {
            return;
        }

        smsCurrentStateService.upsert(event);
        smsRawSseBroadcaster.broadcastRawEvent(event);
    }
}
