package org.telecom_operations_dashboard.internet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.common.dto.internet.HourlyInternetDto;
import org.telecom_operations_dashboard.internet.mapper.InternetDtoMapper;
import org.telecom_operations_dashboard.internet.service.InternetCurrentStateService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class InternetCurrentStateServiceImpl implements InternetCurrentStateService {

    private final InternetDtoMapper internetDtoMapper;
    private final Map<Integer, InternetEvent> currentEvents = new ConcurrentHashMap<>();

    @Override
    public void upsert(InternetEvent event) {
        currentEvents.put(event.cellId(), event);
    }

    @Override
    public List<HourlyInternetDto> getCurrentInternet() {
        return currentEvents.values().stream()
                .map(internetDtoMapper::toHourlyInternetDto)
                .toList();
    }
}
