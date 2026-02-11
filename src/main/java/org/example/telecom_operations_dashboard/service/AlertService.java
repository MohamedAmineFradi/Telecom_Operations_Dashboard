package org.example.telecom_operations_dashboard.service;

import jakarta.annotation.Nullable;
import org.example.telecom_operations_dashboard.dto.AlertDto;
import org.example.telecom_operations_dashboard.dto.CongestionCellDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface AlertService {

    List<AlertDto> getAlertsSince(@Nullable OffsetDateTime since);

    void resolveAlert(Long id);

    List<AlertDto> generateCongestionAlerts(
            OffsetDateTime hour,
            int limit,
            double warningThreshold,
            double criticalThreshold
    );
}
