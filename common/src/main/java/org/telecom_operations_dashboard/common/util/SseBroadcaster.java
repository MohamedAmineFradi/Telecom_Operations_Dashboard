package org.telecom_operations_dashboard.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Utility to safely broadcast events to a collection of SseEmitters.
 */
public class SseBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(SseBroadcaster.class);
    private static final int DEFAULT_MAX_EMITTERS = 250;

    /**
     * Broadcasts data to all emitters in the collection.
     * Removes failed emitters automatically.
     */
    public static <T> void broadcast(Collection<SseEmitter> emitters, String eventName, T data) {
        if (emitters == null || emitters.isEmpty() || data == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data)
                        .id(String.valueOf(System.currentTimeMillis())));
            } catch (IOException | IllegalStateException ex) {
                log.debug("Removing disconnected SSE emitter: {}", ex.getMessage());
                emitters.remove(emitter);
            } catch (Exception ex) {
                log.warn("Failed to send SSE event: {}", ex.getMessage());
                emitters.remove(emitter);
            }
        }
    }

    public static <T> void broadcastJson(Collection<SseEmitter> emitters, String eventName, T data) {
        if (emitters == null || emitters.isEmpty() || data == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data, MediaType.APPLICATION_JSON)
                        .id(String.valueOf(System.currentTimeMillis())));
            } catch (IOException | IllegalStateException ex) {
                log.debug("Removing disconnected SSE emitter: {}", ex.getMessage());
                emitters.remove(emitter);
            } catch (Exception ex) {
                log.warn("Failed to send SSE event: {}", ex.getMessage());
                emitters.remove(emitter);
            }
        }
    }

    public static SseEmitter registerEmitter(List<SseEmitter> emitters, String connectionEventName) {
        return registerEmitter(emitters, connectionEventName, DEFAULT_MAX_EMITTERS);
    }

    public static SseEmitter registerEmitter(List<SseEmitter> emitters, String connectionEventName, int maxEmitters) {
        SseEmitter emitter = new SseEmitter(0L);

        int safeMaxEmitters = Math.max(1, maxEmitters);
        while (emitters.size() >= safeMaxEmitters) {
            SseEmitter dropped = emitters.get(0);
            emitters.remove(dropped);
            try {
                dropped.complete();
            } catch (Exception ignored) {
                // no-op
            }
        }

        emitters.add(emitter);

        Runnable cleanup = () -> emitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                    .name(connectionEventName)
                    .data("connected", MediaType.TEXT_PLAIN));
        } catch (IOException | IllegalStateException ex) {
            cleanup.run();
        }

        return emitter;
    }

    public static <T> boolean sendJsonToEmitter(SseEmitter emitter, String eventName, T data) {
        if (emitter == null || data == null) {
            return false;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data, MediaType.APPLICATION_JSON)
                    .id(String.valueOf(System.currentTimeMillis())));
            return true;
        } catch (IOException | IllegalStateException ex) {
            return false;
        } catch (Exception ex) {
            log.warn("Failed to send replay SSE event: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Broadcasts a single event to all emitters.
     */
    public static void broadcastEvent(Collection<SseEmitter> emitters, SseEmitter.SseEventBuilder eventBuilder) {
        if (emitters == null || emitters.isEmpty() || eventBuilder == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(eventBuilder);
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
            } catch (Exception ex) {
                log.warn("Failed to send custom SSE event: {}", ex.getMessage());
                emitters.remove(emitter);
            }
        }
    }
}
