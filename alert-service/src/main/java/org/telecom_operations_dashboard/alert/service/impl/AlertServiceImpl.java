package org.telecom_operations_dashboard.alert.service.impl;

import jakarta.annotation.Nullable;
import org.telecom_operations_dashboard.common.exception.ResourceNotFoundException;
import org.telecom_operations_dashboard.alert.mapper.AlertMapper;
import org.telecom_operations_dashboard.common.dto.alert.AlertDto;
import org.telecom_operations_dashboard.alert.model.Alert;
import org.telecom_operations_dashboard.alert.repository.AlertRepository;
import org.telecom_operations_dashboard.alert.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.alert.client.RestCellInfoClient;
import org.telecom_operations_dashboard.common.dto.cell.CellDetailsDto;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.service", havingValue = "alert")
public class AlertServiceImpl implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertServiceImpl.class);

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final RestCellInfoClient restCellInfoClient;

    public AlertServiceImpl(
            AlertRepository alertRepository,
            AlertMapper alertMapper,
            RestCellInfoClient restCellInfoClient
    ) {
        this.alertRepository = alertRepository;
        this.alertMapper = alertMapper;
        this.restCellInfoClient = restCellInfoClient;
    }

    @Override
    public List<AlertDto> getAlertsSince(@Nullable OffsetDateTime since) {
        List<Alert> alerts = since != null
            ? alertRepository.findByTimestampAfterOrderByTimestampDesc(since)
            : alertRepository.findAllByOrderByTimestampDesc();

        log.info("Fetched {} alerts since {}", alerts.size(), since);
        return alerts.stream()
            .map(alertMapper::toDto)
            .toList();
    }

    @Override
    public Page<AlertDto> getAlerts(@Nullable OffsetDateTime since, Pageable pageable) {
        Page<Alert> alerts = since != null
            ? alertRepository.findByTimestampAfterOrderByTimestampDesc(since, pageable)
            : alertRepository.findAllByOrderByTimestampDesc(pageable);

        log.info("Fetched page of alerts since {} with page size {}", since, pageable.getPageSize());
        return alerts.map(alertMapper::toDto);
    }

    @Override
    public void resolveAlert(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alert not found: " + id);
        }
        alertRepository.deleteById(id);
        log.info("Resolved alert {}", id);
    }

    @Override
    public void handleCongestionEvent(org.telecom_operations_dashboard.common.dto.event.CongestionEvent event) {
        if ("LOW".equals(event.getSeverity())) {
            return;
        }

        // Avoid duplicate alerts for the same cell and hour
        if (alertRepository.existsByCellIdAndTypeAndTimestamp(event.getCellId(), "CONGESTION", event.getHour())) {
            log.debug("Alert already exists for cell {} at hour {}", event.getCellId(), event.getHour());
            return;
        }

        Alert alert = new Alert();
        alert.setCellId(event.getCellId());
        alert.setType("CONGESTION");
        alert.setSeverity(event.getSeverity());
        alert.setTimestamp(event.getHour());

        // Fetch cell details for location info
        CellDetailsDto cellDetails = restCellInfoClient.fetchCellDetails(event.getCellId());
        String locationInfo = "";
        if (cellDetails != null) {
            locationInfo = String.format(
                " [bounds: %s, centroid: (%.5f, %.5f)]",
                cellDetails.bounds() != null ? cellDetails.bounds() : "N/A",
                cellDetails.centroidX() != null ? cellDetails.centroidX() : 0.0,
                cellDetails.centroidY() != null ? cellDetails.centroidY() : 0.0
            );
        }

        alert.setMessage(
            "Congestion score " + String.format("%.2f", event.getScore()) + "% at hour " + event.getHour() + locationInfo
        );

        alertRepository.save(alert);
        log.info("Asynchronous alert generated for cell {} (Severity: {})", event.getCellId(), event.getSeverity());
    }
}
