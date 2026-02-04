package org.example.telecom_operations_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.telecom_operations_dashboard.dto.AlertDto;
import org.example.telecom_operations_dashboard.controller.util.DateTimeParser;
import org.example.telecom_operations_dashboard.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Validated
public class AlertsController {

    private static final Logger log = LoggerFactory.getLogger(AlertsController.class);

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertDto>> getAlerts(
            @RequestParam(name = "since", required = false) String sinceIso
    ) {
        OffsetDateTime since = sinceIso != null ? DateTimeParser.parse(sinceIso, "since") : null;
        log.info("Alerts requested since {}", sinceIso);
        return ResponseEntity.ok(alertService.getAlertsSince(since));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable("id") Long id) {
        alertService.resolveAlert(id);
        return ResponseEntity.noContent().build();
    }
}
