package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.dto.CellDetailsDto;

public interface CellService {
    CellDetailsDto getCellDetails(Integer cellId);
}
