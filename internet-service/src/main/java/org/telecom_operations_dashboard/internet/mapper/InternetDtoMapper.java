package org.telecom_operations_dashboard.internet.mapper;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.dto.internet.HourlyInternetDto;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.internet.model.HourlyInternetView;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class InternetDtoMapper {

    private final InternetMapper internetMapper;



    public HourlyInternetDto toHourlyInternetDto(HourlyInternetView row) {
        java.time.OffsetDateTime hour = row.getHour() == null ? null : java.time.OffsetDateTime.ofInstant(row.getHour(), java.time.ZoneId.systemDefault());
        return new HourlyInternetDto(
                row.getCellId(),
                hour,
                internetMapper.safe(row.getTotalInternet())
        );
    }
    
    public HourlyInternetDto toHourlyInternetDto(InternetEvent event) {
        if (event == null) return null;
        return new HourlyInternetDto(
            event.cellId(),
            event.hour(),
                internetMapper.safe(event.totalInternet())
        );
    }
}
