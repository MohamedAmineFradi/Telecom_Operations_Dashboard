package org.example.telecom_operations_dashboard.controller;

import org.example.telecom_operations_dashboard.service.TrafficStreamingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stream")
public class StreamingController {

    private final TrafficStreamingService streamingService;

    public StreamingController(TrafficStreamingService s) {
        this.streamingService = s;
    }

    @PostMapping("/trigger-once")
    public void triggerOnce() {
        streamingService.streamNextSlot();
    }
}
