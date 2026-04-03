package org.telecom_operations_dashboard.alert.service;

import jakarta.annotation.Nullable;
import org.telecom_operations_dashboard.common.dto.alert.AlertDto;
import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;
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
