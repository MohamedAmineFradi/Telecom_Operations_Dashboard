package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.dto.CellDetailsDto;
import org.example.telecom_operations_dashboard.dto.GridCellDto;

import java.util.List;

public interface CellService {
    CellDetailsDto getCellDetails(Integer cellId);
    List<GridCellDto> getAllGridCells();
}
