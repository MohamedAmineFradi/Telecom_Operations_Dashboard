package org.telecom_operations_dashboard.traffic.streaming.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.telecom_operations_dashboard.common.dto.event.CongestionEvent;

public final class CongestionEventSerde {

    private CongestionEventSerde() {
    }

    public static Serde<CongestionEvent> create() {
        return Serdes.serdeFrom(new CongestionEventSerializer(), new CongestionEventDeserializer());
    }
}
