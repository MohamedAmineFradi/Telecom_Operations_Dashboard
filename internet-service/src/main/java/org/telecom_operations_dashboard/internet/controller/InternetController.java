package org.telecom_operations_dashboard.internet.controller;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.dto.internet.HourlyInternetDto;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.internet.mapper.InternetDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.io.IOException;

@RestController
@RequestMapping("/api/internet")
@RequiredArgsConstructor
public class InternetController {

    private static final Logger log = LoggerFactory.getLogger(InternetController.class);

    private final InternetDtoMapper internetDtoMapper;
    private final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<Integer, InternetEvent> currentEvents = new ConcurrentHashMap<>();
    private final ScheduledExecutorService sseScheduler = Executors.newScheduledThreadPool(2);

    @PreDestroy
    void shutdownSseScheduler() {
        sseScheduler.shutdownNow();
    }


        @KafkaListener(
            topics = "activity.internet",
            groupId = "${app.kafka.sse.group-id:${spring.application.name:${app.service:service}}-sse-${HOSTNAME:${random.uuid}}}"
        )
    public void listenInternetEvents(@org.springframework.messaging.handler.annotation.Payload InternetEvent event) {
        if (event != null && event.cellId() != null) {
            currentEvents.put(event.cellId(), event);
        }
        if (event != null) {
            for (SseEmitter emitter : rawEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("internet-raw")
                        .data(event));
                } catch (IOException | IllegalStateException ex) {
                    rawEmitters.remove(emitter);
                }
            }
        }
    }


    @GetMapping(path = "/raw/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRawInternet() {
        SseEmitter emitter = new SseEmitter(0L);
        rawEmitters.add(emitter);
        Runnable cleanup = () -> rawEmitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());
        log.info("New raw Internet SSE client connected.");
        return emitter;
    }

    @GetMapping(path = "/current/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentInternet(
            @RequestParam(name = "intervalMs", defaultValue = "60000") long intervalMs
    ) {
        log.info("SSE stream opened for /api/internet/current/stream intervalMs={}", intervalMs);
        SseEmitter emitter = new SseEmitter(0L);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> taskRef = new AtomicReference<>();

        Runnable shutdown = () -> {
            if (closed.compareAndSet(false, true)) {
                ScheduledFuture<?> task = taskRef.get();
                if (task != null) {
                    task.cancel(true);
                }
                log.info("SSE stream closed for /api/internet/current/stream");
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
                List<HourlyInternetDto> payload = currentEvents.values().stream()
                    .map(internetDtoMapper::toHourlyInternetDto)
                    .toList();
                emitter.send(SseEmitter.event()
                        .name("internet-current")
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
