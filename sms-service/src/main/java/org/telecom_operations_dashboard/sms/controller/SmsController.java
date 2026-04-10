package org.telecom_operations_dashboard.sms.controller;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.controller.AbstractSseController;
import org.telecom_operations_dashboard.sms.mapper.SmsDtoMapper;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController extends AbstractSseController {

    private final SmsDtoMapper smsDtoMapper;
    private final ConcurrentMap<Integer, SmsEvent> currentEvents = new ConcurrentHashMap<>();

    @KafkaListener(
        topics = "${kafka.topics.sms:activity.sms}",
        groupId = "${app.kafka.sse.group-id:${spring.application.name:${app.service:service}}-sse-${HOSTNAME:${random.uuid}}}"
    )
    public void listenSmsEvents(@Payload SmsEvent event) {
        if (event == null || event.cellId() == null) {
            return;
        }

        currentEvents.put(event.cellId(), event);
        broadcastToRawEmitters("sms-raw", event);
    }

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawSms() {
        return createRawEmitter("raw SMS");
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentSms(
            @RequestParam(name = "intervalMs", defaultValue = "5000") long intervalMs
    ) {
        return createScheduledEmitter(
                intervalMs,
                "sms-current",
                "/api/sms/current/stream",
                () -> currentEvents.values().stream()
                    .map(smsDtoMapper::toHourlySmsDto)
                    .toList()
        );
    }
}
