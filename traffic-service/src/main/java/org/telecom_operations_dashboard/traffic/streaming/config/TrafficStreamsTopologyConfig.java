package org.telecom_operations_dashboard.traffic.streaming.config;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.telecom_operations_dashboard.common.util.NormalizationUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Configuration
@ConditionalOnProperty(name = "app.streaming.enabled", havingValue = "true")
public class TrafficStreamsTopologyConfig {

    @Autowired
    private TrafficCongestionProperties congestionProperties;

    @Bean
    public KStream<String, TrafficEvent> trafficHourlyCellAggregationTopology(
            StreamsBuilder streamsBuilder,
            org.springframework.core.env.Environment environment
    ) {
        String topic = environment.getProperty("kafka.topics.traffic", "activity.traffic");
        var trafficEventSerde = TrafficEventSerde.create();

        KStream<String, TrafficEvent> source = streamsBuilder.stream(
                topic,
                Consumed.with(Serdes.String(), trafficEventSerde)
        );

        KStream<String, TrafficEvent> processedSource = source
                .filter((key, event) -> event != null && event.getHour() != null && event.getCellId() != null)
                .mapValues(this::normalizeEvent);

        // Real-time alerting (immediate per-event check)
        processedSource
                .mapValues(this::toCongestionEvent)
                .filter((key, event) -> !"LOW".equals(event.getSeverity()))
                .to(TrafficStreamsStoreNames.CONGESTION_TOPIC,
                        org.apache.kafka.streams.kstream.Produced.with(Serdes.String(), CongestionEventSerde.create()));

        // Hourly aggregation (for persistence and stateful heatmap queries)
        processedSource
                .selectKey((key, event) -> compositeKey(event.getHour(), event.getCellId()))
                .groupByKey(Grouped.with(Serdes.String(), trafficEventSerde))
                .aggregate(
                        TrafficEvent::new,
                        (aggKey, incoming, aggregate) -> accumulate(incoming, aggregate),
                        Materialized.<String, TrafficEvent, KeyValueStore<org.apache.kafka.common.utils.Bytes, byte[]>>as(
                                TrafficStreamsStoreNames.TRAFFIC_HOURLY_CELL_STORE
                        )
                        .withKeySerde(Serdes.String())
                        .withValueSerde(trafficEventSerde)
                );

        return source;
    }

    private org.telecom_operations_dashboard.common.dto.event.CongestionEvent toCongestionEvent(TrafficEvent event) {
        BigDecimal total = safe(event.getTotalActivity());
        String severity;
        double score;

        BigDecimal lowMax = congestionProperties.normalizedLowMax();
        BigDecimal mediumMax = congestionProperties.normalizedMediumMax();

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

        return new org.telecom_operations_dashboard.common.dto.event.CongestionEvent(
                event.getCellId(),
                event.getHour(),
                total,
                score,
                severity
        );
    }

    private TrafficEvent normalizeEvent(TrafficEvent event) {
        TrafficEvent normalized = new TrafficEvent();
        normalized.setHour(NormalizationUtils.truncateToHour(event.getHour()));
        normalized.setCellId(event.getCellId());
        normalized.setTotalSmsin(safe(event.getTotalSmsin()));
        normalized.setTotalSmsout(safe(event.getTotalSmsout()));
        normalized.setTotalCallin(safe(event.getTotalCallin()));
        normalized.setTotalCallout(safe(event.getTotalCallout()));
        normalized.setTotalInternet(safe(event.getTotalInternet()));
        normalized.setTotalActivity(safe(event.getTotalActivity()));
        return normalized;
    }

    private TrafficEvent accumulate(TrafficEvent incoming, TrafficEvent aggregate) {
        aggregate.setHour(incoming.getHour());
        aggregate.setCellId(incoming.getCellId());
        aggregate.setTotalSmsin(safe(aggregate.getTotalSmsin()).add(safe(incoming.getTotalSmsin())));
        aggregate.setTotalSmsout(safe(aggregate.getTotalSmsout()).add(safe(incoming.getTotalSmsout())));
        aggregate.setTotalCallin(safe(aggregate.getTotalCallin()).add(safe(incoming.getTotalCallin())));
        aggregate.setTotalCallout(safe(aggregate.getTotalCallout()).add(safe(incoming.getTotalCallout())));
        aggregate.setTotalInternet(safe(aggregate.getTotalInternet()).add(safe(incoming.getTotalInternet())));
        aggregate.setTotalActivity(totalActivity(aggregate));
        return aggregate;
    }

    private BigDecimal totalActivity(TrafficEvent event) {
        return safe(event.getTotalSmsin())
                .add(safe(event.getTotalSmsout()))
                .add(safe(event.getTotalCallin()))
                .add(safe(event.getTotalCallout()))
                .add(safe(event.getTotalInternet()));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String compositeKey(OffsetDateTime hour, Integer cellId) {
        return hour + "|" + cellId;
    }
}
