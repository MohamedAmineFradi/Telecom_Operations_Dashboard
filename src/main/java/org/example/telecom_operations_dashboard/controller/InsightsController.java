package org.example.telecom_operations_dashboard.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.controller.util.DateTimeParser;
import org.example.telecom_operations_dashboard.dto.MobilityCellProvinceFlowDto;
import org.example.telecom_operations_dashboard.dto.MobilityProvinceSummaryDto;
import org.example.telecom_operations_dashboard.dto.NetworkStatsDto;
import org.example.telecom_operations_dashboard.repository.AlertRepository;
import org.example.telecom_operations_dashboard.repository.TrafficRecordRepository;
import org.example.telecom_operations_dashboard.service.MobilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InsightsController {

    private static final Logger log = LoggerFactory.getLogger(InsightsController.class);

    private final AlertRepository alertRepository;
    private final TrafficRecordRepository trafficRecordRepository;
    private final MobilityService mobilityService;

    @GetMapping("/mobility/flows")
    public ResponseEntity<List<MobilityCellProvinceFlowDto>> getMobilityFlows(
            @RequestParam("hour") @NotBlank String hourIso,
            @RequestParam(name = "cellId", required = false) Integer cellId,
            @RequestParam(name = "provincia", required = false) String provincia,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit) {
        OffsetDateTime hour = DateTimeParser.parse(hourIso, "hour");
        log.info("Mobility flows requested at {} (cellId={}, provincia={}, limit={})", hourIso, cellId, provincia, limit);
        return ResponseEntity.ok(mobilityService.getMobilityFlowsAtHour(hour, cellId, provincia, limit));
    }

    @GetMapping("/mobility/province-summary")
    public ResponseEntity<List<MobilityProvinceSummaryDto>> getProvinceSummaries(
            @RequestParam("hour") @NotBlank String hourIso,
            @RequestParam(name = "provincia", required = false) String provincia,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit) {
        OffsetDateTime hour = DateTimeParser.parse(hourIso, "hour");
        log.info("Province summaries requested at {} (provincia={}, limit={})", hourIso, provincia, limit);
        return ResponseEntity.ok(mobilityService.getProvinceSummariesAtHour(hour, provincia, limit));
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
