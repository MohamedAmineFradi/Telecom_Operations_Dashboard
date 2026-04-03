package org.telecom_operations_dashboard.traffic.mapper;

import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TrafficEventMapper {

    public TrafficEvent fromSms(SmsEvent event) {
        TrafficEvent traffic = base(event.hour(), event.cellId());
        traffic.setTotalSmsin(event.totalSmsin());
        traffic.setTotalSmsout(event.totalSmsout());
        return traffic;
    }

    public TrafficEvent fromCall(CallEvent event) {
        TrafficEvent traffic = base(event.hour(), event.cellId());
        traffic.setTotalCallin(event.totalCallin());
        traffic.setTotalCallout(event.totalCallout());
        return traffic;
    }

    public TrafficEvent fromInternet(InternetEvent event) {
        TrafficEvent traffic = base(event.hour(), event.cellId());
        traffic.setTotalInternet(event.totalInternet());
        return traffic;
    }

    public TrafficEvent accumulate(TrafficEvent incoming, TrafficEvent aggregate) {
        aggregate.setCellId(incoming.getCellId());
        aggregate.setHour(incoming.getHour());

        aggregate.setTotalSmsin(nullSafe(aggregate.getTotalSmsin()).add(nullSafe(incoming.getTotalSmsin())));
        aggregate.setTotalSmsout(nullSafe(aggregate.getTotalSmsout()).add(nullSafe(incoming.getTotalSmsout())));
        aggregate.setTotalCallin(nullSafe(aggregate.getTotalCallin()).add(nullSafe(incoming.getTotalCallin())));
        aggregate.setTotalCallout(nullSafe(aggregate.getTotalCallout()).add(nullSafe(incoming.getTotalCallout())));
        aggregate.setTotalInternet(nullSafe(aggregate.getTotalInternet()).add(nullSafe(incoming.getTotalInternet())));
        aggregate.setTotalActivity(totalActivity(aggregate));

        return aggregate;
    }

    private TrafficEvent base(java.time.OffsetDateTime hour, Integer cellId) {
        TrafficEvent traffic = new TrafficEvent();
        traffic.setHour(hour);
        traffic.setCellId(cellId);
        return traffic;
    }

    private BigDecimal totalActivity(TrafficEvent traffic) {
        return nullSafe(traffic.getTotalSmsin())
                .add(nullSafe(traffic.getTotalSmsout()))
                .add(nullSafe(traffic.getTotalCallin()))
                .add(nullSafe(traffic.getTotalCallout()))
                .add(nullSafe(traffic.getTotalInternet()));
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}