package org.telecom_operations_dashboard.traffic.streaming.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;

public final class TrafficEventSerde {

    private TrafficEventSerde() {
    }

    public static Serde<TrafficEvent> create() {
        return Serdes.serdeFrom(new TrafficEventSerializer(), new TrafficEventDeserializer());
    }
}
