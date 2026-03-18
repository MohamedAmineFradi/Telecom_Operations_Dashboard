package org.example.telecom_operations_dashboard.streaming.mapper;

import org.example.telecom_operations_dashboard.streaming.dto.event.MobilityEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MobilityEventMapper {

    public boolean hasRequiredFields(MobilityEvent value) {
        return value != null
                && value.datetime() != null
                && value.cellId() != null
                && value.provincia() != null
                && !value.provincia().isBlank()
                && value.cell2province() != null
                && value.province2cell() != null;
    }

    public boolean hasPositiveFlow(MobilityEvent value) {
        return value != null
                && value.cell2province() != null
                && value.province2cell() != null
                && (value.cell2province().compareTo(BigDecimal.ZERO) > 0
                || value.province2cell().compareTo(BigDecimal.ZERO) > 0);
    }
}