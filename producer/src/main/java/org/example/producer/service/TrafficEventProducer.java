package org.example.producer.service;

import org.telecom_operations_dashboard.common.dto.event.TrafficEvent;
import org.example.producer.model.HourlyTrafficRecord;
import org.example.producer.repository.HourlyTrafficRecordRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TrafficEventProducer extends AbstractReplayProducer<HourlyTrafficRecord> {

    private static final String WATERMARK_STREAM = "traffic";
    private static final String TOPIC_TRAFFIC  = "activity.traffic";

    private final HourlyTrafficRecordRepository repository;

    public TrafficEventProducer(HourlyTrafficRecordRepository repository,
                                KafkaTemplate<String, Object> kafkaTemplate,
                                WatermarkStore watermarkStore,
                                org.telecom_operations_dashboard.common.config.SimulationProperties simulationProperties) {
        super(kafkaTemplate, watermarkStore, simulationProperties);
        this.repository = repository;
    }


    @Override
    protected List<HourlyTrafficRecord> fetchRecords(OffsetDateTime start, OffsetDateTime end, Pageable pageable) {
        return repository.findByHourGreaterThanEqualAndHourLessThanOrderByHourAscCellIdAsc(start, end, pageable);
    }

    @Override
    protected List<CompletableFuture<?>> sendKafkaEvents(HourlyTrafficRecord r) {
        String key = String.valueOf(r.getCellId());
        return List.of(
            kafkaTemplate.send(TOPIC_TRAFFIC, key,
                    new TrafficEvent(r.getHour(), r.getCellId(),
                            r.getTotalSmsin(), r.getTotalSmsout(),
                            r.getTotalCallin(), r.getTotalCallout(),
                            r.getTotalInternet(), r.getTotalActivity()))
        );
    }

    @Override
    protected String getStreamName() {
        return WATERMARK_STREAM;
    }
}