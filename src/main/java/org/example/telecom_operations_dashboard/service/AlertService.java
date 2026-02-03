package org.example.telecom_operations_dashboard.service;

import jakarta.annotation.Nullable;
import org.example.telecom_operations_dashboard.dto.AlertDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface AlertService {

    List<AlertDto> getAlertsSince(@Nullable OffsetDateTime since);
}
