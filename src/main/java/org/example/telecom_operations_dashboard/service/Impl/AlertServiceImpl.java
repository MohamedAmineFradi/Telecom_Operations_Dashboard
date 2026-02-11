package org.example.telecom_operations_dashboard.service.Impl;

import jakarta.annotation.Nullable;
import org.example.telecom_operations_dashboard.controller.exception.ResourceNotFoundException;
import org.example.telecom_operations_dashboard.dto.AlertDto;
import org.example.telecom_operations_dashboard.dto.CongestionCellDto;
import org.example.telecom_operations_dashboard.model.Alert;
import org.example.telecom_operations_dashboard.repository.AlertRepository;
import org.example.telecom_operations_dashboard.service.AlertService;
import org.example.telecom_operations_dashboard.service.TrafficService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertServiceImpl.class);

    private final AlertRepository alertRepository;
    private final TrafficService trafficService;

    public AlertServiceImpl(AlertRepository alertRepository, TrafficService trafficService) {
        this.alertRepository = alertRepository;
        this.trafficService = trafficService;
    }

    @Override
    public List<AlertDto> getAlertsSince(@Nullable OffsetDateTime since) {
        List<Alert> alerts = since != null
            ? alertRepository.findByTimestampAfterOrderByTimestampDesc(since)
            : alertRepository.findAllByOrderByTimestampDesc();

        log.info("Fetched {} alerts since {}", alerts.size(), since);
        return alerts.stream()
            .map(a -> new AlertDto(
                a.getId(),
                a.getCellId(),
                a.getType(),
                a.getSeverity(),
                a.getMessage(),
                a.getTimestamp() != null ? a.getTimestamp().toString() : null
            ))
            .toList();
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
    public List<AlertDto> generateCongestionAlerts(
            OffsetDateTime hour,
            int limit,
            double warningThreshold,
            double criticalThreshold
    ) {
        List<CongestionCellDto> congestion = trafficService.getCongestionAtHour(hour, limit, warningThreshold, criticalThreshold);
        List<Alert> created = congestion.stream()
                .filter(c -> !"NORMAL".equals(c.severity()))
                .filter(c -> !alertRepository.existsByCellIdAndTypeAndTimestamp(c.cellId(), "CONGESTION", hour))
                .map(c -> {
                    Alert alert = new Alert();
                    alert.setCellId(c.cellId());
                    alert.setType("CONGESTION");
                    alert.setSeverity(c.severity());
                    alert.setMessage("Congestion score " + String.format("%.2f", c.score()) + "% at hour " + hour);
                    alert.setTimestamp(hour);
                    return alert;
                })
                .toList();

        if (!created.isEmpty()) {
            alertRepository.saveAll(created);
        }

        log.info("Generated {} congestion alerts at hour {}", created.size(), hour);
        return created.stream()
                .map(a -> new AlertDto(
                        a.getId(),
                        a.getCellId(),
                        a.getType(),
                        a.getSeverity(),
                        a.getMessage(),
                        a.getTimestamp() != null ? a.getTimestamp().toString() : null
                ))
                .toList();
    }
}
