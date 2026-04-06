package org.example.producer.service;

import org.telecom_operations_dashboard.common.dto.event.MobilityEvent;
import org.example.producer.model.MobilityRecord;
import org.example.producer.repository.MobilityRecordRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MobilityEventProducer extends AbstractReplayProducer<MobilityRecord> {

    private static final String TOPIC_MOBILITY = "activity.mobility";
    private static final String WATERMARK_STREAM = "mobility";

    private final MobilityRecordRepository repository;

    public MobilityEventProducer(MobilityRecordRepository repository,
                                 KafkaTemplate<String, Object> kafkaTemplate,
                                 WatermarkStore watermarkStore,
                                 org.telecom_operations_dashboard.common.config.SimulationProperties simulationProperties) {
        super(kafkaTemplate, watermarkStore, simulationProperties);
        this.repository = repository;
    }

    @Override
    protected List<MobilityRecord> fetchRecords(OffsetDateTime start, OffsetDateTime end, Pageable pageable) {
        return repository.findByDatetimeGreaterThanEqualAndDatetimeLessThanOrderByDatetimeAscCellIdAscProvinciaAsc(start, end, pageable);
    }

    @Override
    protected List<CompletableFuture<?>> sendKafkaEvents(MobilityRecord m) {
        String key = m.getCellId() + ":" + m.getProvincia();
        return List.of(
                kafkaTemplate.send(TOPIC_MOBILITY, key,
                        new MobilityEvent(m.getDatetime(), m.getCellId(),
                                m.getProvincia(), m.getCell2province(), m.getProvince2cell()))
        );
    }

    @Override
    protected String getStreamName() {
        return WATERMARK_STREAM;
    }
}