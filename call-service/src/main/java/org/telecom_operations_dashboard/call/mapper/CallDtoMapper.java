package org.telecom_operations_dashboard.call.mapper;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.telecom_operations_dashboard.call.model.HourlyCallView;
import org.telecom_operations_dashboard.common.dto.call.HourlyCallDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CallDtoMapper {

    private final CallMapper callMapper;

    public HourlyCallDto toHourlyCallDto(HourlyCallView row) {
        // Convert Instant to OffsetDateTime using system default zone
        java.time.OffsetDateTime hour = row.getHour() == null ? null : java.time.OffsetDateTime.ofInstant(row.getHour(), java.time.ZoneId.systemDefault());
        return new HourlyCallDto(
                row.getCellId(),
                hour,
                callMapper.safe(row.getTotalCallin()),
                callMapper.safe(row.getTotalCallout())
        );
    }

    public HourlyCallDto toHourlyCallDto(CallEvent event) {
        return new HourlyCallDto(
                event.cellId(),
                event.hour(),
                callMapper.safe(event.totalCallin()),
                callMapper.safe(event.totalCallout())
        );
    }

}
