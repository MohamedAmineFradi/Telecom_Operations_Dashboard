package org.telecom_operations_dashboard.alert.service;

import jakarta.annotation.Nullable;
import org.telecom_operations_dashboard.common.dto.alert.AlertDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.telecom_operations_dashboard.common.dto.event.CongestionEvent;

import java.time.OffsetDateTime;
import java.util.List;

public interface AlertService {

    List<AlertDto> getAlertsSince(@Nullable OffsetDateTime since);

    Page<AlertDto> getAlerts(@Nullable OffsetDateTime since, Pageable pageable);

    void resolveAlert(Long id);

    void handleCongestionEvent( CongestionEvent event);
}
