package org.example.telecom_operations_dashboard.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.CellTimeseriesPointDto;
import org.example.telecom_operations_dashboard.dto.CongestionCellDto;
import org.example.telecom_operations_dashboard.dto.HeatmapCellDto;
import org.example.telecom_operations_dashboard.dto.HourlyCellDto;
import org.example.telecom_operations_dashboard.dto.HourlyTrafficSummaryDto;
import org.example.telecom_operations_dashboard.dto.TopCellDto;
import org.example.telecom_operations_dashboard.controller.util.DateTimeParser;
import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.example.telecom_operations_dashboard.service.TrafficService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
@Validated
public class TrafficController {

    private static final Logger log = LoggerFactory.getLogger(TrafficController.class);

    private final TrafficService trafficService;

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapCellDto>> getHeatmap(
            @RequestParam("datetime") @NotBlank String datetimeIso
    ) {
        OffsetDateTime datetime = DateTimeParser.parse(datetimeIso, "datetime");
        log.info("Heatmap requested at {}", datetimeIso);
        return ResponseEntity.ok(trafficService.getHeatmapAt(datetime));
    }

    @GetMapping("/top-cells")
    public ResponseEntity<List<TopCellDto>> getTopCells(
            @RequestParam("hour") @NotBlank String hourIso,
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) int limit) {
        OffsetDateTime hour = DateTimeParser.parse(hourIso, "hour");
        log.info("Top cells requested at {} with limit {}", hourIso, limit);
        return ResponseEntity.ok(trafficService.getTopCells(hour, limit));
    }

    @GetMapping("/hourly")
    public ResponseEntity<List<HourlyCellDto>> getAllCellsAtHour(
            @RequestParam("hour") @NotBlank String hourIso) {
        OffsetDateTime hour = DateTimeParser.parse(hourIso, "hour");
        log.info("Hourly traffic requested at {}", hourIso);
        return ResponseEntity.ok(trafficService.getAllCellsAtHour(hour));
    }

    @GetMapping("/hourly-summary")
    public ResponseEntity<HourlyTrafficSummaryDto> getHourlySummary(
            @RequestParam("hour") @NotBlank String hourIso) {
        OffsetDateTime hour = DateTimeParser.parse(hourIso, "hour");
        log.info("Hourly summary requested at {}", hourIso);
        return ResponseEntity.ok(trafficService.getHourlySummaryAtHour(hour));
    }

    @GetMapping("/congestion")
    public ResponseEntity<List<CongestionCellDto>> getCongestion(
            @RequestParam("hour") @NotBlank String hourIso,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) int limit,
            @RequestParam(name = "warn", defaultValue = "70") double warn,
            @RequestParam(name = "crit", defaultValue = "90") double crit) {
        OffsetDateTime hour = DateTimeParser.parse(hourIso, "hour");
        log.info("Congestion requested at {} (limit={}, warn={}, crit={})", hourIso, limit, warn, crit);
        return ResponseEntity.ok(trafficService.getCongestionAtHour(hour, limit, warn, crit));
    }

    @GetMapping("/cells/{cellId}/timeseries")
    public ResponseEntity<List<CellTimeseriesPointDto>> getCellTimeseries(
            @PathVariable("cellId") @Positive Integer cellId,
            @RequestParam("from") @NotBlank String fromIso,
            @RequestParam("to") @NotBlank String toIso,
            @RequestParam(name = "step", defaultValue = "hour")
            @Pattern(regexp = "hour|day|minute", message = "step must be hour, day, or minute") String step
    ) {
        OffsetDateTime from = DateTimeParser.parse(fromIso, "from");
        OffsetDateTime to = DateTimeParser.parse(toIso, "to");
        log.info("Timeseries requested cellId={}, from={}, to={}, step={}", cellId, fromIso, toIso, step);
        return ResponseEntity.ok(trafficService.getCellTimeseries(cellId, from, to, step));
    }
}

