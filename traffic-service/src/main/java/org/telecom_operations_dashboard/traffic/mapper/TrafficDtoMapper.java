package org.telecom_operations_dashboard.traffic.mapper;

import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;
import org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;
import org.telecom_operations_dashboard.traffic.config.TrafficCongestionProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TrafficDtoMapper {

    public HourlyTrafficDto toHourlyTrafficDto(TrafficEvent event) {
        if (event == null) return null;
        return new HourlyTrafficDto(event.getHour(), event.getCellId(), event.getTotalActivity());
    }

    public CongestionCellDto toCongestionCellDto(TrafficEvent event, TrafficCongestionProperties properties) {
        if (event == null) return null;
        
        BigDecimal total = NormalizationUtils.safe(event.getTotalActivity());
        BigDecimal lowMax = properties.normalizedLowMax();
        BigDecimal mediumMax = properties.normalizedMediumMax();

        String severity;
        double score;

        if (total.compareTo(lowMax) <= 0) {
            severity = "LOW";
            score = 33.0;
        } else if (total.compareTo(mediumMax) <= 0) {
            severity = "MEDIUM";
            score = 66.0;
        } else {
            severity = "HIGH";
            score = 100.0;
        }

        return new CongestionCellDto(event.getCellId(), total, score, severity);
    }
}
