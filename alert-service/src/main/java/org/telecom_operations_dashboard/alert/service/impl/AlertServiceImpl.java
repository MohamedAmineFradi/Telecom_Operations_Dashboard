package org.telecom_operations_dashboard.alert.service.impl;

import jakarta.annotation.Nullable;
import org.telecom_operations_dashboard.common.exception.ResourceNotFoundException;
import org.telecom_operations_dashboard.alert.mapper.AlertMapper;
import org.telecom_operations_dashboard.common.dto.alert.AlertDto;
import org.telecom_operations_dashboard.alert.model.Alert;
import org.telecom_operations_dashboard.alert.model.AlertSeverity;
import org.telecom_operations_dashboard.alert.repository.AlertRepository;
import org.telecom_operations_dashboard.alert.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.alert.client.RestCellInfoClient;
import org.telecom_operations_dashboard.common.dto.cell.CellDetailsDto;
import org.telecom_operations_dashboard.alert.service.EmailService;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.service", havingValue = "alert")
public class AlertServiceImpl implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertServiceImpl.class);
    private static final String ALERT_TYPE_CONGESTION = "CONGESTION";

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final RestCellInfoClient restCellInfoClient;
    private final EmailService emailService;
    private final AlertSseBroadcaster alertSseBroadcaster;

    public AlertServiceImpl(
            AlertRepository alertRepository,
            AlertMapper alertMapper,
            RestCellInfoClient restCellInfoClient,
            EmailService emailService,
            AlertSseBroadcaster alertSseBroadcaster
    ) {
        this.alertRepository = alertRepository;
        this.alertMapper = alertMapper;
        this.restCellInfoClient = restCellInfoClient;
        this.emailService = emailService;
        this.alertSseBroadcaster = alertSseBroadcaster;
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
    public List<AlertDto> getHighAlertsSnapshot(int limit) {
        return getAlertsSnapshot(AlertSeverity.HIGH, limit);
    }

    @Override
    public List<AlertDto> getCriticalAlertsSnapshot(int limit) {
        return getAlertsSnapshot(AlertSeverity.CRITICAL, limit);
    }

    private List<AlertDto> getAlertsSnapshot(AlertSeverity severity, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return alertRepository
                .findBySeverityIgnoreCaseOrderByTimestampDesc(severity.name(), PageRequest.of(0, safeLimit))
                .stream()
                .map(alertMapper::toDto)
                .toList();
    }

    @Override
    public void resolveAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));

        AlertDto alertDto = alertMapper.toDto(alert);
        alertRepository.delete(alert);
        alertSseBroadcaster.broadcastResolvedAlert(alertDto);
        log.info("Resolved alert {}", id);
    }

    @Override
    public void handleCongestionEvent(org.telecom_operations_dashboard.common.dto.event.CongestionEvent event) {
        AlertSeverity incomingSeverity = AlertSeverity.from(event.getSeverity());
        if (!incomingSeverity.isStreamable()) {
            log.debug("Ignoring non-streamable severity '{}' for cell {}", event.getSeverity(), event.getCellId());
            return;
        }

        Alert existing = alertRepository
                .findFirstByCellIdAndTypeAndTimestamp(event.getCellId(), ALERT_TYPE_CONGESTION, event.getHour())
                .orElse(null);

        if (existing != null) {
            AlertSeverity currentSeverity = AlertSeverity.from(existing.getSeverity());
            if (incomingSeverity.rank() <= currentSeverity.rank()) {
                log.debug("Alert already exists for cell {} at hour {} with severity {}", event.getCellId(), event.getHour(), currentSeverity);
                return;
            }

            existing.setSeverity(incomingSeverity.name());
            existing.setMessage(buildAlertMessage(event));

            Alert updated = alertRepository.save(existing);
            AlertDto alertDto = alertMapper.toDto(updated);
            publishSeverityEvents(alertDto, incomingSeverity, currentSeverity);

            if (incomingSeverity == AlertSeverity.HIGH && currentSeverity.rank() < AlertSeverity.HIGH.rank()) {
                emailService.sendHighCongestionAlert(
                        String.valueOf(event.getCellId()),
                        incomingSeverity.name(),
                        updated.getMessage()
                );
            }

            return;
        }

        Alert alert = new Alert();
        alert.setCellId(event.getCellId());
        alert.setType(ALERT_TYPE_CONGESTION);
        alert.setSeverity(incomingSeverity.name());
        alert.setTimestamp(event.getHour());
        alert.setMessage(buildAlertMessage(event));

        Alert savedAlert = alertRepository.save(alert);
        log.info("Asynchronous alert generated for cell {} (Severity: {})", event.getCellId(), incomingSeverity.name());

        AlertDto alertDto = alertMapper.toDto(savedAlert);
        publishSeverityEvents(alertDto, incomingSeverity, null);

        if (incomingSeverity == AlertSeverity.HIGH) {
            emailService.sendHighCongestionAlert(
                    String.valueOf(event.getCellId()),
                    incomingSeverity.name(),
                    alert.getMessage()
            );
        }
    }

    private void publishSeverityEvents(AlertDto alertDto, AlertSeverity incomingSeverity, @Nullable AlertSeverity previousSeverity) {
        if (incomingSeverity == AlertSeverity.CRITICAL) {
            alertSseBroadcaster.broadcastCriticalAlert(alertDto);
            if (previousSeverity != null && previousSeverity != AlertSeverity.CRITICAL) {
                alertSseBroadcaster.broadcastCriticalTransition(alertDto, previousSeverity.name());
            }
        } else if (incomingSeverity == AlertSeverity.HIGH) {
            alertSseBroadcaster.broadcastHighAlert(alertDto);
        }
    }

    private String buildAlertMessage(org.telecom_operations_dashboard.common.dto.event.CongestionEvent event) {
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

        return "Congestion score " + String.format("%.2f", event.getScore()) + "% at hour " + event.getHour() + locationInfo;
    }
}
