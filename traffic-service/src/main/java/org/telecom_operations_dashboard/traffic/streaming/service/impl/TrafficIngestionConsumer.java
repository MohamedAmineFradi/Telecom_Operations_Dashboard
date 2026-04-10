package org.telecom_operations_dashboard.traffic.streaming.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.telecom_operations_dashboard.common.dto.traffic.CongestionCellDto;
import org.telecom_operations_dashboard.common.dto.traffic.HourlyTrafficDto;
import org.telecom_operations_dashboard.traffic.mapper.TrafficDtoMapper;
import org.telecom_operations_dashboard.traffic.streaming.config.TrafficCongestionProperties;

@Service
@RequiredArgsConstructor
public class TrafficIngestionConsumer {

    private final TrafficRawSseBroadcaster trafficRawSseBroadcaster;
    private final TrafficDtoMapper trafficDtoMapper;
    private final TrafficCongestionProperties congestionProperties;

    @KafkaListener(
            id = "traffic-ingestion-listener",
            topics = "${kafka.topics.traffic:activity.total}",
            groupId = "${app.kafka.raw.group-id:traffic-raw-group}"
    )
    public void onTrafficEvent(TrafficEvent event) {
        if (event == null) {
            return;
        }

        // 1. Broadcast Raw Event
        trafficRawSseBroadcaster.broadcastRawEvent(event);

        // 2. Broadcast Real-Time Heatmap Update
        HourlyTrafficDto hourlyDto = trafficDtoMapper.toHourlyTrafficDto(event);
        if (hourlyDto != null) {
            trafficRawSseBroadcaster.broadcastHeatmapUpdate(hourlyDto);
        }

        // 3. Broadcast Real-Time Congestion Update
        CongestionCellDto congestionDto = trafficDtoMapper.toCongestionCellDto(event, congestionProperties);
        if (congestionDto != null) {
            trafficRawSseBroadcaster.broadcastCongestionUpdate(congestionDto);
        }
    }
}
