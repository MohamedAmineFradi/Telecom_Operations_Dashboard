package org.telecom_operations_dashboard.traffic.mapper;

import org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.springframework.stereotype.Component;

@Component
public class TrafficDtoMapper {

    public HourlyTrafficDto toHourlyTrafficDto(TrafficEvent event) {
        if (event == null) return null;
        return new HourlyTrafficDto(
                event.getHour(),
                event.getCellId(),
                event.getTotalSmsin(),
                event.getTotalSmsout(),
                event.getTotalCallin(),
                event.getTotalCallout(),
                event.getTotalInternet(),
                event.getTotalActivity()
        );
    }
}
