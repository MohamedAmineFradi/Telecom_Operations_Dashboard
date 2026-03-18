package org.example.telecom_operations_dashboard.streaming.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.example.telecom_operations_dashboard.streaming.dto.event.CallEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.InternetEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.MobilityEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.SmsEvent;
import org.example.telecom_operations_dashboard.streaming.dto.event.TrafficEvent;

import java.time.OffsetDateTime;

/**
 * Extracts Kafka Streams event-time from payload datetime fields.
 * Falls back to Kafka record timestamp (or partition time when unavailable).
 */
public class EventDateTimeTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        OffsetDateTime dateTime = extractDateTime(record.value());
        if (dateTime != null) {
            return dateTime.toInstant().toEpochMilli();
        }

        long recordTimestamp = record.timestamp();
        return recordTimestamp >= 0 ? recordTimestamp : partitionTime;
    }

    private OffsetDateTime extractDateTime(Object value) {
        if (value instanceof SmsEvent event) {
            return event.datetime();
        }
        if (value instanceof CallEvent event) {
            return event.datetime();
        }
        if (value instanceof InternetEvent event) {
            return event.datetime();
        }
        if (value instanceof MobilityEvent event) {
            return event.datetime();
        }
        if (value instanceof TrafficEvent event) {
            return event.getDatetime();
        }
        return null;
    }
}