package org.telecom_operations_dashboard.sms.mapper;

import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.common.dto.sms.HourlySmsDto;
import org.telecom_operations_dashboard.sms.model.HourlySmsView;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;
import org.springframework.stereotype.Component;



@Component
public class SmsDtoMapper {

    public HourlySmsDto toHourlySmsDto(HourlySmsView row) {
        java.time.OffsetDateTime hour = row.getHour() == null ? null : java.time.OffsetDateTime.ofInstant(row.getHour(), java.time.ZoneId.systemDefault());
        return new HourlySmsDto(
                row.getCellId(),
                hour,
                NormalizationUtils.safe(row.getTotalSmsin()),
                NormalizationUtils.safe(row.getTotalSmsout()),
                NormalizationUtils.safe(row.getTotalActivity())
        );
    }

    public HourlySmsDto toHourlySmsDto(SmsEvent event) {
        if (event == null) return null;
        return new HourlySmsDto(
                event.cellId(),
                event.hour(),
                NormalizationUtils.safe(event.totalSmsin()),
                NormalizationUtils.safe(event.totalSmsout()),
                NormalizationUtils.safe(event.totalSmsin()).add(NormalizationUtils.safe(event.totalSmsout()))
        );
    }
}
