package org.telecom_operations_dashboard.alert.mapper;

import org.telecom_operations_dashboard.common.dto.alert.AlertDto;
import org.telecom_operations_dashboard.alert.model.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertDto toDto(Alert alert) {
        return new AlertDto(
                alert.getId(),
                alert.getCellId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getTimestamp() != null ? alert.getTimestamp().toString() : null
        );
    }
}
