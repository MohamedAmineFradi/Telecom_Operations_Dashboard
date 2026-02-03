package org.example.telecom_operations_dashboard.service.Impl;

import jakarta.annotation.Nullable;
import org.example.telecom_operations_dashboard.dto.AlertDto;
import org.example.telecom_operations_dashboard.model.Alert;
import org.example.telecom_operations_dashboard.repository.AlertRepository;
import org.example.telecom_operations_dashboard.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertServiceImpl.class);

    private final AlertRepository alertRepository;

    public AlertServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
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
}
