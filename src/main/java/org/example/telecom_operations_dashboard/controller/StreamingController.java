package org.example.telecom_operations_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.KafkaClientConfigDto;
import org.example.telecom_operations_dashboard.dto.StreamSlotResultDto;
import org.example.telecom_operations_dashboard.dto.StreamStatusDto;
import org.example.telecom_operations_dashboard.controller.util.DateTimeParser;
import org.example.telecom_operations_dashboard.service.TrafficStreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@Validated
public class StreamingController {

    private static final Logger log = LoggerFactory.getLogger(StreamingController.class);
    private static final int STREAM_SLOT_INTERVAL_MS = 10_000;
    private static final int EXPECTED_EVENTS_PER_SLOT = 50;

    private final TrafficStreamingService streamingService;

    @Value("${traffic.kafka.topic}")
    private String kafkaTopic;

    @Value("${traffic.kafka.client-bootstrap-servers:localhost:9092}")
    private String clientBootstrapServers;

    @PostMapping("/trigger-once")
    public void triggerOnce() {
        log.info("Stream trigger-once requested");
        streamingService.streamSlot(null);
    }

    @PostMapping("/slot")
    public ResponseEntity<StreamSlotResultDto> streamOneSlot(
            @RequestParam(name = "datetime", required = false) String datetimeIso
    ) {
        OffsetDateTime datetime = datetimeIso != null ? DateTimeParser.parse(datetimeIso, "datetime") : null;
        log.info("Stream slot requested for {}", datetimeIso);
        return ResponseEntity.ok(streamingService.streamSlot(datetime));
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startStreaming() {
        log.info("Continuous streaming started");
        streamingService.enableStreaming();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "streaming_started");
        response.put("message", "Continuous streaming enabled");
        response.put("timestamp", OffsetDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopStreaming() {
        log.info("Continuous streaming stopped");
        streamingService.disableStreaming();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "streaming_stopped");
        response.put("message", "Continuous streaming disabled");
        response.put("timestamp", OffsetDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<StreamStatusDto> getStreamStatus() {
        log.info("Stream status requested");
        return ResponseEntity.ok(streamingService.getStatus());
    }

    @GetMapping("/is-active")
    public ResponseEntity<Map<String, Object>> isStreamingActive() {
        Map<String, Object> response = new HashMap<>();
        response.put("active", streamingService.isStreamingEnabled());
        response.put("timestamp", OffsetDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client-config")
    public ResponseEntity<KafkaClientConfigDto> getKafkaClientConfig() {
        log.info("Kafka client config requested for UI/client integration");
        KafkaClientConfigDto config = new KafkaClientConfigDto(
                Arrays.stream(clientBootstrapServers.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList(),
                kafkaTopic,
                "string",
                "json",
                "ISO-8601 offset datetime",
                EXPECTED_EVENTS_PER_SLOT,
                STREAM_SLOT_INTERVAL_MS
        );
        return ResponseEntity.ok(config);
    }
}
