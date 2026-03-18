package org.example.telecom_operations_dashboard.streaming.service;

import org.example.telecom_operations_dashboard.dto.streaming.StreamStatusDto;

public interface TrafficStreamingService {

    StreamStatusDto getStatus();

    void enableStreaming();

    void disableStreaming();

    boolean isStreamingEnabled();
}