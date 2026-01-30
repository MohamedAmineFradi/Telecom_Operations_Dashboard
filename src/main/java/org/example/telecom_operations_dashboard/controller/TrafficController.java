package org.example.telecom_operations_dashboard.controller;

import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.example.telecom_operations_dashboard.service.TrafficService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private final TrafficService trafficService;

    public TrafficController(TrafficService trafficService) {
        this.trafficService = trafficService;
    }

    @GetMapping("/top-cells")
    public List<HourlyTrafficView> getTopCells(
            @RequestParam("hour") String hourIso,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        OffsetDateTime hour = parseHour(hourIso);
        return trafficService.getTopCellsAtHour(hour, limit);
    }



    private OffsetDateTime parseHour(String hourIso) {
        try {
            return OffsetDateTime.parse(hourIso);
        } catch (DateTimeParseException ex) {
            LocalDateTime local = LocalDateTime.parse(hourIso);
            return local.atOffset(ZoneOffset.UTC);
        }
    }
}

