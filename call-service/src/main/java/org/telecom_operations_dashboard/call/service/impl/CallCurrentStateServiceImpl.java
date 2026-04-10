package org.telecom_operations_dashboard.call.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.call.mapper.CallDtoMapper;
import org.telecom_operations_dashboard.call.service.CallCurrentStateService;
import org.telecom_operations_dashboard.common.dto.call.HourlyCallDto;
import org.telecom_operations_dashboard.common.dto.event.CallEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CallCurrentStateServiceImpl implements CallCurrentStateService {

    private final CallDtoMapper callDtoMapper;
    private final Map<Integer, CallEvent> currentEvents = new ConcurrentHashMap<>();

    @Override
    public void upsert(CallEvent event) {
        currentEvents.put(event.cellId(), event);
    }

    @Override
    public List<HourlyCallDto> getCurrentCalls() {
        return currentEvents.values().stream()
                .map(callDtoMapper::toHourlyCallDto)
                .toList();
    }
}
