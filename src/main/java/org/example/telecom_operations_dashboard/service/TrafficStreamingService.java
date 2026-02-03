package org.example.telecom_operations_dashboard.service;

import jakarta.annotation.Nullable;
import org.example.telecom_operations_dashboard.dto.StreamSlotResultDto;
import org.example.telecom_operations_dashboard.dto.StreamStatusDto;

import java.time.OffsetDateTime;

public interface TrafficStreamingService {

    StreamSlotResultDto streamSlot(@Nullable OffsetDateTime slotDatetime);

    StreamStatusDto getStatus();
}
