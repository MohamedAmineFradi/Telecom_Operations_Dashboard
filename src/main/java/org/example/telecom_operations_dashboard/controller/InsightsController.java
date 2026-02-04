package org.example.telecom_operations_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.MobilityFlowDto;
import org.example.telecom_operations_dashboard.dto.NetworkStatsDto;
import org.example.telecom_operations_dashboard.repository.AlertRepository;
import org.example.telecom_operations_dashboard.repository.TrafficRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InsightsController {

    private static final Logger log = LoggerFactory.getLogger(InsightsController.class);

    private final AlertRepository alertRepository;
    private final TrafficRecordRepository trafficRecordRepository;

    @GetMapping("/mobility")
    public ResponseEntity<List<MobilityFlowDto>> getMobilityFlows() {
        log.info("Mobility flows requested");
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/stats")
    public ResponseEntity<NetworkStatsDto> getNetworkStats() {
        log.info("Network stats requested");
        long totalAlerts = alertRepository.count();
        long totalCells = trafficRecordRepository.countDistinctCells();
        long totalTrafficRecords = trafficRecordRepository.count();
        OffsetDateTime latest = trafficRecordRepository.findLatestDatetime();

        return ResponseEntity.ok(new NetworkStatsDto(
                totalAlerts,
                totalCells,
                totalTrafficRecords,
                latest != null ? latest.toString() : null
        ));
    }
}
