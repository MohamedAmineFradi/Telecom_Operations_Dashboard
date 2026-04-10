package org.telecom_operations_dashboard.sms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.common.dto.sms.HourlySmsDto;
import org.telecom_operations_dashboard.sms.mapper.SmsDtoMapper;
import org.telecom_operations_dashboard.sms.service.SmsCurrentStateService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SmsCurrentStateServiceImpl implements SmsCurrentStateService {

    private final SmsDtoMapper smsDtoMapper;
    private final Map<Integer, SmsEvent> currentEvents = new ConcurrentHashMap<>();

    @Override
    public void upsert(SmsEvent event) {
        currentEvents.put(event.cellId(), event);
    }

    @Override
    public List<HourlySmsDto> getCurrentSms() {
        return currentEvents.values().stream()
                .map(smsDtoMapper::toHourlySmsDto)
                .toList();
    }
}
