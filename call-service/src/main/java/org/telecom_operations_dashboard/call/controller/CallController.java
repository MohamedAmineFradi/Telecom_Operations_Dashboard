package org.telecom_operations_dashboard.call.controller;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.dto.call.HourlyCallDto;
import org.telecom_operations_dashboard.call.mapper.CallDtoMapper;
import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.io.IOException;

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
public class CallController {

    private static final Logger log = LoggerFactory.getLogger(CallController.class);

    private final CallDtoMapper callDtoMapper;
    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<Integer, CallEvent> currentEvents = new ConcurrentHashMap<>();
    private final ScheduledExecutorService sseScheduler = Executors.newScheduledThreadPool(2);

    @PreDestroy
    void shutdownSseScheduler() {
        sseScheduler.shutdownNow();
    }


        @KafkaListener(
            topics = "activity.call",
            groupId = "${app.kafka.sse.group-id:${spring.application.name:${app.service:service}}-sse-${HOSTNAME:${random.uuid}}}"
        )
    public void listenCallEvents(@Payload CallEvent event) {
        if (event != null && event.cellId() != null) {
            currentEvents.put(event.cellId(), event);
        }

        if (event != null) {
            for (SseEmitter emitter : rawEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("call-raw")
                            .data(event));
                } catch (IOException | IllegalStateException ex) {
                    rawEmitters.remove(emitter);
                }
            }
        }
    }


    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawCall() {
        SseEmitter emitter = new SseEmitter(0L);
        rawEmitters.add(emitter);
        
        Runnable cleanup = () -> rawEmitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());
        
        log.info("New raw Call SSE client connected.");
        return emitter;
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentCall(
            @RequestParam(name = "intervalMs", defaultValue = "60000") long intervalMs
    ) {
        log.info("SSE stream opened for /api/call/current/stream intervalMs={}", intervalMs);
        SseEmitter emitter = new SseEmitter(0L);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> taskRef = new AtomicReference<>();

        Runnable shutdown = () -> {
            if (closed.compareAndSet(false, true)) {
                ScheduledFuture<?> task = taskRef.get();
                if (task != null) {
                    task.cancel(true);
                }
                log.info("SSE stream closed for /api/call/current/stream");
            }
        };

        emitter.onCompletion(shutdown);
        emitter.onTimeout(() -> {
            shutdown.run();
            emitter.complete();
        });
        emitter.onError(error -> shutdown.run());

        ScheduledFuture<?> task = sseScheduler.scheduleAtFixedRate(() -> {
            if (closed.get()) return;
            try {
                List<HourlyCallDto> payload = currentEvents.values().stream()
                        .map(callDtoMapper::toHourlyCallDto)
                        .toList();
                emitter.send(SseEmitter.event()
                        .name("call-current")
                        .data(payload));
            } catch (IOException | IllegalStateException ex) {
                log.debug("SSE client disconnected");
                shutdown.run();
            } catch (Exception ex) {
                log.error("Failed to push SSE update", ex);
                shutdown.run();
                emitter.completeWithError(ex);
            }
        }, 0, Math.max(intervalMs, 1000), TimeUnit.MILLISECONDS);
        taskRef.set(task);

        return emitter;
    }
}
