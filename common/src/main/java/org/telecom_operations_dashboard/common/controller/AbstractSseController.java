package org.telecom_operations_dashboard.common.controller;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.telecom_operations_dashboard.common.util.SseBroadcaster;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Abstract base controller for SSE-based real-time updates.
 */
public abstract class AbstractSseController {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final List<SseEmitter> rawEmitters = new CopyOnWriteArrayList<>();
    protected final ScheduledExecutorService sseScheduler;

    protected AbstractSseController() {
        this.sseScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "SSE-" + getClass().getSimpleName() + "-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    public void shutdownSseScheduler() {
        log.info("Shutting down SSE scheduler for {}", getClass().getSimpleName());
        sseScheduler.shutdownNow();
    }

    /**
     * Registers a new raw stream emitter.
     */
    protected SseEmitter createRawEmitter(String clientName) {
        SseEmitter emitter = new SseEmitter(0L);
        rawEmitters.add(emitter);

        Runnable cleanup = () -> rawEmitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        log.info("New {} SSE client connected.", clientName);
        return emitter;
    }

    /**
     * Creates a scheduled SSE emitter that periodically sends data from a supplier.
     */
    protected SseEmitter createScheduledEmitter(
            long intervalMs,
            String eventName,
            String endpoint,
            Supplier<Object> dataSupplier
    ) {
        return createScheduledEmitter(intervalMs, 0L, eventName, endpoint, dataSupplier, false);
    }

    /**
     * Creates a scheduled SSE emitter with custom timeout and optional empty filtering.
     */
    protected SseEmitter createScheduledEmitter(
            long intervalMs,
            long timeoutMs,
            String eventName,
            String endpoint,
            Supplier<Object> dataSupplier,
            boolean skipEmpty
    ) {
        log.info("SSE stream opened for {} intervalMs={} timeout={}ms", endpoint, intervalMs, timeoutMs);
        
        long safeInterval = Math.max(intervalMs, 1000);
        SseEmitter emitter = new SseEmitter(timeoutMs);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> taskRef = new AtomicReference<>();

        Runnable shutdown = () -> {
            if (closed.compareAndSet(false, true)) {
                ScheduledFuture<?> task = taskRef.get();
                if (task != null) {
                    task.cancel(true);
                }
                log.info("SSE stream closed for {}", endpoint);
            }
        };

        emitter.onCompletion(shutdown);
        emitter.onTimeout(() -> {
            shutdown.run();
            try {
                emitter.complete();
            } catch (Exception ignored) {}
        });
        emitter.onError(error -> shutdown.run());

        // Flush an initial lightweight event so clients/gateways receive bytes
        // immediately even when the first data query is expensive.
        try {
            emitter.send(SseEmitter.event()
                    .name("stream-open")
                    .data("ok")
                    .id(String.valueOf(System.currentTimeMillis())));
        } catch (Exception ex) {
            shutdown.run();
            completeEmitter(emitter, ex);
            return emitter;
        }

        ScheduledFuture<?> task = sseScheduler.scheduleAtFixedRate(() -> {
            if (closed.get()) return;
            try {
                Object payload = dataSupplier.get();
                if (payload != null) {
                    if (skipEmpty && payload instanceof java.util.Collection && ((java.util.Collection<?>) payload).isEmpty()) {
                        return;
                    }
                    emitter.send(SseEmitter.event()
                            .name(eventName)
                            .data(payload)
                            .id(String.valueOf(System.currentTimeMillis())));
                }
            } catch (Exception ex) {
                log.debug("SSE push failed for {}: {}", endpoint, ex.getMessage());
                shutdown.run();
                completeEmitter(emitter, ex);
            }
        }, 0, safeInterval, TimeUnit.MILLISECONDS);
        
        taskRef.set(task);
        return emitter;
    }

    private void completeEmitter(SseEmitter emitter, Exception ex) {
        try {
            if (isClientDisconnect(ex)) {
                emitter.complete();
            } else {
                emitter.completeWithError(ex);
            }
        } catch (Exception ignored) {
            // No-op: client may already be gone.
        }
    }

    private boolean isClientDisconnect(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof IOException) {
                String msg = current.getMessage();
                if (msg == null) {
                    return true;
                }
                String lower = msg.toLowerCase();
                if (lower.contains("broken pipe")
                        || lower.contains("connection reset")
                        || lower.contains("connection abort")
                        || lower.contains("connection closed")) {
                    return true;
                }
            }

            String type = current.getClass().getName();
            if (type.contains("ClientAbortException") || type.contains("EofException")) {
                return true;
            }

            current = current.getCause();
        }
        return false;
    }

    /**
     * Broadcasts a raw event to all registered raw emitters.
     */
    protected void broadcastToRawEmitters(String eventName, Object event) {
        SseBroadcaster.broadcast(rawEmitters, eventName, event);
    }
}
