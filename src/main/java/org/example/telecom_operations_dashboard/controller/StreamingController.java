package org.example.telecom_operations_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.StreamSlotResultDto;
import org.example.telecom_operations_dashboard.dto.StreamStatusDto;
import org.example.telecom_operations_dashboard.controller.util.DateTimeParser;
import org.example.telecom_operations_dashboard.service.TrafficStreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@Validated
public class StreamingController {

    private static final Logger log = LoggerFactory.getLogger(StreamingController.class);

    private final TrafficStreamingService streamingService;

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

    @GetMapping("/status")
    public ResponseEntity<StreamStatusDto> getStreamStatus() {
        log.info("Stream status requested");
        return ResponseEntity.ok(streamingService.getStatus());
    }
}
