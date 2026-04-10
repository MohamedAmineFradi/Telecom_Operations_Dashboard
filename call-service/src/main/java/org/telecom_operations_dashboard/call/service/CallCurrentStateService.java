package org.telecom_operations_dashboard.call.service;

import org.telecom_operations_dashboard.common.dto.call.HourlyCallDto;
import org.telecom_operations_dashboard.common.dto.event.CallEvent;

import java.util.List;

public interface CallCurrentStateService {

    void upsert(CallEvent event);

    List<HourlyCallDto> getCurrentCalls();
}
