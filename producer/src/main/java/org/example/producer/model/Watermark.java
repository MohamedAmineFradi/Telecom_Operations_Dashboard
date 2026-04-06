package org.example.producer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;


@Entity
@Table(name = "watermarks")
public class Watermark {

    @Id
    @Column(name = "stream_name", length = 64)
    private String streamName;

    @Column(name = "last_processed", nullable = false)
    private OffsetDateTime lastProcessed;

    public Watermark() {}

    public Watermark(String streamName, OffsetDateTime lastProcessed) {
        this.streamName    = streamName;
        this.lastProcessed = lastProcessed;
    }

    public String getStreamName() { return streamName; }
    public void setStreamName(String streamName) { this.streamName = streamName; }

    public OffsetDateTime getLastProcessed() { return lastProcessed; }
    public void setLastProcessed(OffsetDateTime lastProcessed) { this.lastProcessed = lastProcessed; }
}
