package org.example.telecom_operations_dashboard.service;

import jakarta.annotation.Nullable;
import org.example.telecom_operations_dashboard.dto.alert.AlertDto;
import org.example.telecom_operations_dashboard.dto.traffic.CongestionCellDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface AlertService {

    List<AlertDto> getAlertsSince(@Nullable OffsetDateTime since);

    Page<AlertDto> getAlerts(@Nullable OffsetDateTime since, Pageable pageable);

    void resolveAlert(Long id);

    List<AlertDto> generateCongestionAlerts(
            OffsetDateTime hour,
            int limit,
            double warningThreshold,
            double criticalThreshold
    );
}
