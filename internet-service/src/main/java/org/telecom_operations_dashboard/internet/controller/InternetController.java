package org.telecom_operations_dashboard.internet.controller;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.controller.AbstractSseController;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.internet.mapper.InternetDtoMapper;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/internet")
@RequiredArgsConstructor
public class InternetController extends AbstractSseController {

    private final InternetDtoMapper internetDtoMapper;
    private final ConcurrentMap<Integer, InternetEvent> currentEvents = new ConcurrentHashMap<>();

    @KafkaListener(
        topics = "${kafka.topics.internet:activity.internet}",
        groupId = "${app.kafka.sse.group-id:${spring.application.name:${app.service:service}}-sse-${HOSTNAME:${random.uuid}}}"
    )
    public void listenInternetEvents(@Payload InternetEvent event) {
        if (event == null || event.cellId() == null) {
            return;
        }

        currentEvents.put(event.cellId(), event);
        broadcastToRawEmitters("internet-raw", event);
    }

    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawInternet() {
        return createRawEmitter("raw Internet");
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentInternet(
            @RequestParam(name = "intervalMs", defaultValue = "60000") long intervalMs
    ) {
        return createScheduledEmitter(
                intervalMs,
                "internet-current",
                "/api/internet/current/stream",
                () -> currentEvents.values().stream()
                    .map(internetDtoMapper::toHourlyInternetDto)
                    .toList()
        );
    }
}
