package org.example.producer.service;

import org.telecom_operations_dashboard.common.dto.event.CallEvent;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
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
    private static final String TOPIC_CALL = "activity.call";
    private static final String TOPIC_SMS = "activity.sms";
    private static final String TOPIC_INTERNET = "activity.internet";
    private static final String TOPIC_TOTAL = "activity.total";

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
            kafkaTemplate.send(TOPIC_CALL, key,
                new CallEvent(
                    r.getHour(),
                    r.getCellId(),
                    r.getTotalCallin(),
                    r.getTotalCallout()
                )),
            kafkaTemplate.send(TOPIC_SMS, key,
                new SmsEvent(
                    r.getHour(),
                    r.getCellId(),
                    r.getTotalSmsin(),
                    r.getTotalSmsout()
                )),
            kafkaTemplate.send(TOPIC_INTERNET, key,
                new InternetEvent(
                    r.getHour(),
                    r.getCellId(),
                    r.getTotalInternet()
                )),
            kafkaTemplate.send(TOPIC_TOTAL, key,
                new TrafficEvent(
                    r.getHour(),
                    r.getCellId(),
                    r.getTotalActivity()
                ))
        );
    }

    @Override
    protected String getStreamName() {
        return WATERMARK_STREAM;
    }
}