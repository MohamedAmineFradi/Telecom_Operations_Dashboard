package org.example.producer.service;

import org.example.producer.model.Watermark;
import org.example.producer.repository.WatermarkRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * DB-backed watermark store.
 * Progress is persisted to the {@code watermarks} table so replay survives
 * container restarts without replaying from the beginning every time.
 */
@Component
public class WatermarkStore {

    private final WatermarkRepository repository;

    public WatermarkStore(WatermarkRepository repository) {
        this.repository = repository;
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