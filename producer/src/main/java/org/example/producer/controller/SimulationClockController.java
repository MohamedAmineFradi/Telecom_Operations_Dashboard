package org.example.producer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.example.producer.service.SimulationClockSseBroadcaster;

@RestController
@RequestMapping("/api/simulation-clock")
@RequiredArgsConstructor
public class SimulationClockController {

    private final SimulationClockSseBroadcaster simulationClockSseBroadcaster;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSimulationClock() {
        return simulationClockSseBroadcaster.registerStream();
    }
}
