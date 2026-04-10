package org.telecom_operations_dashboard.sms.service;

import org.telecom_operations_dashboard.common.dto.event.SmsEvent;
import org.telecom_operations_dashboard.common.dto.sms.HourlySmsDto;

import java.util.List;

public interface SmsCurrentStateService {

    void upsert(SmsEvent event);

    List<HourlySmsDto> getCurrentSms();
}
