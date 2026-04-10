package org.telecom_operations_dashboard.call.mapper;

import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.telecom_operations_dashboard.call.model.HourlyCallView;
import org.telecom_operations_dashboard.common.dto.call.HourlyCallDto;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;
import org.springframework.stereotype.Component;

@Component
public class CallDtoMapper {

    public HourlyCallDto toHourlyCallDto(HourlyCallView row) {
        // Convert Instant to OffsetDateTime using system default zone
        java.time.OffsetDateTime hour = row.getHour() == null ? null : java.time.OffsetDateTime.ofInstant(row.getHour(), java.time.ZoneId.systemDefault());
        return new HourlyCallDto(
                row.getCellId(),
                hour,
                NormalizationUtils.safe(row.getTotalCallin()),
                NormalizationUtils.safe(row.getTotalCallout())
        );
    }

    public HourlyCallDto toHourlyCallDto(CallEvent event) {
        return new HourlyCallDto(
                event.cellId(),
                event.hour(),
                NormalizationUtils.safe(event.totalCallin()),
                NormalizationUtils.safe(event.totalCallout())
        );
    }

}
