package org.example.producer.service;

import jakarta.annotation.PostConstruct;
import org.example.producer.model.Watermark;
import org.example.producer.repository.WatermarkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * DB-backed watermark store.
 * Progress is persisted to the {@code watermarks} table so replay survives
 * container restarts without replaying from the beginning every time.
 */
@Component
public class WatermarkStore {

    private static final Logger log = LoggerFactory.getLogger(WatermarkStore.class);

    private final WatermarkRepository repository;
    private final boolean resetOnStartup;

    public WatermarkStore(WatermarkRepository repository,
            @Value("${producer.watermarks.reset-on-startup:false}") boolean resetOnStartup) {
        this.repository = repository;
        this.resetOnStartup = resetOnStartup;
    }

    @PostConstruct
    public void resetWatermarksIfRequested() {
        if (!resetOnStartup) {
            return;
        }

        repository.deleteAll();
        log.info("Cleared persisted replay watermarks before startup");
    }

    /**
     * Returns the last persisted watermark for {@code stream}, or
     * {@code initialValue} if this stream has never been checkpointed.
     */
    public OffsetDateTime getLast(String stream, OffsetDateTime initialValue) {
        return repository.findById(stream)
                .map(Watermark::getLastProcessed)
                .orElse(initialValue);
    }

    /** Persists a new watermark for {@code stream}. */
    public void update(String stream, OffsetDateTime newWatermark) {
        repository.save(new Watermark(stream, newWatermark));
    }
}