package org.telecom_operations_dashboard.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;

/**
 * Utility to safely broadcast events to a collection of SseEmitters.
 */
public class SseBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(SseBroadcaster.class);

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
