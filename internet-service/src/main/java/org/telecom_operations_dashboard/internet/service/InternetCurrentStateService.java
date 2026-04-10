package org.telecom_operations_dashboard.internet.service;

import org.telecom_operations_dashboard.common.dto.event.InternetEvent;
import org.telecom_operations_dashboard.common.dto.internet.HourlyInternetDto;

import java.util.List;

public interface InternetCurrentStateService {

    void upsert(InternetEvent event);

    List<HourlyInternetDto> getCurrentInternet();
}
