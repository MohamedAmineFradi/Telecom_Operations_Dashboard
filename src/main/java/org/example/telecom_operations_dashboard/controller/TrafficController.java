package org.example.telecom_operations_dashboard.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.CellTimeseriesPointDto;
import org.example.telecom_operations_dashboard.dto.HeatmapCellDto;
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
    public List<HourlyTrafficView> getTopCells(
            @RequestParam("hour") @NotBlank String hourIso,
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) int limit) {
        OffsetDateTime hour = DateTimeParser.parse(hourIso, "hour");
        log.info("Top cells requested at {} with limit {}", hourIso, limit);
        return trafficService.getTopCellsAtHour(hour, limit);
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

