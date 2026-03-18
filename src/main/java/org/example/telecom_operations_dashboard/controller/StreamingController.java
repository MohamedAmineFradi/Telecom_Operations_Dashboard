package org.example.telecom_operations_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.streaming.StreamStatusDto;
import org.example.telecom_operations_dashboard.streaming.service.TrafficStreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@Validated
@ConditionalOnProperty(prefix = "app.streaming", name = "enabled", havingValue = "true")
public class StreamingController {

    private static final Logger log = LoggerFactory.getLogger(StreamingController.class);

    private final TrafficStreamingService streamingService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startStreaming() {
        log.info("Kafka Streams start requested");
        streamingService.enableStreaming();
        return ResponseEntity.ok(Map.of(
                "status", "streaming_started",
                "message", "Kafka Streams topology started",
                "timestamp", OffsetDateTime.now().toString()
        ));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopStreaming() {
        log.info("Kafka Streams stop requested");
        streamingService.disableStreaming();
        return ResponseEntity.ok(Map.of(
                "status", "streaming_stopped",
                "message", "Kafka Streams topology stopped",
                "timestamp", OffsetDateTime.now().toString()
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<StreamStatusDto> getStreamStatus() {
        return ResponseEntity.ok(streamingService.getStatus());
    }

    @GetMapping("/is-active")
    public ResponseEntity<Map<String, Object>> isStreamingActive() {
        return ResponseEntity.ok(Map.of(
                "active", streamingService.isStreamingEnabled(),
                "timestamp", OffsetDateTime.now().toString()
        ));
    }
}
