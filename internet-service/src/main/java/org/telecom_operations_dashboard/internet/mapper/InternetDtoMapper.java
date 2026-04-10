package org.telecom_operations_dashboard.internet.mapper;

import org.telecom_operations_dashboard.common.dto.internet.HourlyInternetDto;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.internet.model.HourlyInternetView;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;
import org.springframework.stereotype.Component;

@Component
public class InternetDtoMapper {


    public HourlyInternetDto toHourlyInternetDto(HourlyInternetView row) {
        java.time.OffsetDateTime hour = row.getHour() == null ? null : java.time.OffsetDateTime.ofInstant(row.getHour(), java.time.ZoneId.systemDefault());
        return new HourlyInternetDto(
                row.getCellId(),
                hour,
                NormalizationUtils.safe(row.getTotalInternet())
        );
    }
    
    public HourlyInternetDto toHourlyInternetDto(InternetEvent event) {
        if (event == null) return null;
        return new HourlyInternetDto(
            event.cellId(),
            event.hour(),
                NormalizationUtils.safe(event.totalInternet())
        );
    }
}
