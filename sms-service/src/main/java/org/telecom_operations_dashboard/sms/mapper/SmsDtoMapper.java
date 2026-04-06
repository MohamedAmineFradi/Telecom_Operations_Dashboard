package org.telecom_operations_dashboard.sms.mapper;

import lombok.RequiredArgsConstructor;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.common.dto.sms.HourlySmsDto;
import org.telecom_operations_dashboard.sms.mapper.SmsMapper;
import org.telecom_operations_dashboard.sms.model.HourlySmsView;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class SmsDtoMapper {

    private final SmsMapper smsMapper;


    public HourlySmsDto toHourlySmsDto(HourlySmsView row) {
        java.time.OffsetDateTime hour = row.getHour() == null ? null : java.time.OffsetDateTime.ofInstant(row.getHour(), java.time.ZoneId.systemDefault());
        return new HourlySmsDto(
                row.getCellId(),
                hour,
                smsMapper.safe(row.getTotalSmsin()),
                smsMapper.safe(row.getTotalSmsout()),
                smsMapper.safe(row.getTotalActivity())
        );
    }

    public HourlySmsDto toHourlySmsDto(SmsEvent event) {
        if (event == null) return null;
        return new HourlySmsDto(
                event.cellId(),
                event.hour(),
                smsMapper.safe(event.totalSmsin()),
                smsMapper.safe(event.totalSmsout()),
                smsMapper.safe(event.totalSmsin()).add(smsMapper.safe(event.totalSmsout()))
        );
    }
}
