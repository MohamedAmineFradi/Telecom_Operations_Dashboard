package org.telecom_operations_dashboard.call.controller;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.controller.AbstractSseController;
import org.telecom_operations_dashboard.call.mapper.CallDtoMapper;
import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
public class CallController extends AbstractSseController {

    private final CallDtoMapper callDtoMapper;
    private final ConcurrentMap<Integer, CallEvent> currentEvents = new ConcurrentHashMap<>();

    @KafkaListener(
        topics = "activity.traffic",
        groupId = "${app.kafka.sse.group-id:${spring.application.name:${app.service:service}}-sse-${HOSTNAME:${random.uuid}}}"
    )
    public void listenCallEvents(@Payload TrafficEvent event) {
        if (event == null || event.getCellId() == null) {
            return;
        }

        CallEvent callEvent = new CallEvent(
                event.getHour(),
                event.getCellId(),
                event.getTotalCallin(),
                event.getTotalCallout()
        );

        currentEvents.put(event.getCellId(), callEvent);
        broadcastToRawEmitters("call-raw", callEvent);
    }

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawCall() {
        return createRawEmitter("raw Call");
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentCall(
            @RequestParam(name = "intervalMs", defaultValue = "60000") long intervalMs
    ) {
        return createScheduledEmitter(
                intervalMs,
                "call-current",
                "/api/call/current/stream",
                () -> currentEvents.values().stream()
                        .map(callDtoMapper::toHourlyCallDto)
                        .toList()
        );
    }
}
