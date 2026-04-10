package org.telecom_operations_dashboard.alert.controller;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.alert.service.impl.AlertSseBroadcaster;
import org.telecom_operations_dashboard.common.util.DateTimeParser;
import org.telecom_operations_dashboard.alert.service.AlertService;
import org.telecom_operations_dashboard.common.dto.alert.AlertDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Validated
@ConditionalOnProperty(name = "app.service", havingValue = "alert")
public class AlertsController {

    private static final Logger log = LoggerFactory.getLogger(AlertsController.class);

    private final AlertService alertService;
    private final AlertSseBroadcaster alertSseBroadcaster;

    @GetMapping
    public ResponseEntity<List<AlertDto>> getAlerts(
            @RequestParam(name = "since", required = false) String sinceIso
    ) {
        OffsetDateTime since = sinceIso == null || sinceIso.isBlank() ? null : DateTimeParser.parse(sinceIso, "since");
        log.info("Alerts requested since {}", sinceIso);
        return ResponseEntity.ok(alertService.getAlertsSince(since));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<AlertDto>> getAlertsPaged(
            @RequestParam(name = "since", required = false) String sinceIso,
            Pageable pageable
    ) {
        OffsetDateTime since = sinceIso == null || sinceIso.isBlank() ? null : DateTimeParser.parse(sinceIso, "since");
        log.info("Paginated alerts requested since {} with page size {}", sinceIso, pageable.getPageSize());
        return ResponseEntity.ok(alertService.getAlerts(since, pageable));
    }

    @GetMapping(path = "/high/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamHighAlerts() {
        return alertSseBroadcaster.registerHighStream();
    }

    @GetMapping(path = "/critical/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCriticalAlerts() {
        return alertSseBroadcaster.registerCriticalStream();
    }

    @GetMapping(path = "/high/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AlertDto>> getHighAlertsSnapshot(
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit
    ) {
        return ResponseEntity.ok(alertService.getHighAlertsSnapshot(limit));
    }

    @GetMapping(path = "/critical/snapshot", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AlertDto>> getCriticalAlertsSnapshot(
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit
    ) {
        return ResponseEntity.ok(alertService.getCriticalAlertsSnapshot(limit));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable("id") Long id) {
        alertService.resolveAlert(id);
        return ResponseEntity.noContent().build();
    }
}
