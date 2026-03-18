package org.example.telecom_operations_dashboard.streaming.mapper;

import org.example.telecom_operations_dashboard.streaming.dto.event.CallEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.InternetEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.SmsEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.TrafficEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TrafficEventMapper {

    public TrafficEvent fromSms(SmsEvent event) {
        TrafficEvent traffic = base(event.datetime(), event.cellId(), event.countrycode());
        traffic.setTotalSmsin(event.smsin());
        traffic.setTotalSmsout(event.smsout());
        return traffic;
    }

    public TrafficEvent fromCall(CallEvent event) {
        TrafficEvent traffic = base(event.datetime(), event.cellId(), event.countrycode());
        traffic.setTotalCallin(event.callin());
        traffic.setTotalCallout(event.callout());
        return traffic;
    }

    public TrafficEvent fromInternet(InternetEvent event) {
        TrafficEvent traffic = base(event.datetime(), event.cellId(), event.countrycode());
        traffic.setTotalInternet(event.internet());
        return traffic;
    }

    public TrafficEvent accumulate(TrafficEvent incoming, TrafficEvent aggregate) {
        aggregate.setCellId(incoming.getCellId());
        aggregate.setDatetime(incoming.getDatetime());
        if (aggregate.getCountrycode() == null) {
            aggregate.setCountrycode(incoming.getCountrycode());
        }

        aggregate.setTotalSmsin(nullSafe(aggregate.getTotalSmsin()).add(nullSafe(incoming.getTotalSmsin())));
        aggregate.setTotalSmsout(nullSafe(aggregate.getTotalSmsout()).add(nullSafe(incoming.getTotalSmsout())));
        aggregate.setTotalCallin(nullSafe(aggregate.getTotalCallin()).add(nullSafe(incoming.getTotalCallin())));
        aggregate.setTotalCallout(nullSafe(aggregate.getTotalCallout()).add(nullSafe(incoming.getTotalCallout())));
        aggregate.setTotalInternet(nullSafe(aggregate.getTotalInternet()).add(nullSafe(incoming.getTotalInternet())));
        aggregate.setTotalActivity(totalActivity(aggregate));

        return aggregate;
    }

    private TrafficEvent base(java.time.OffsetDateTime datetime, Integer cellId, Integer countryCode) {
        TrafficEvent traffic = new TrafficEvent();
        traffic.setDatetime(datetime);
        traffic.setCellId(cellId);
        traffic.setCountrycode(countryCode);
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