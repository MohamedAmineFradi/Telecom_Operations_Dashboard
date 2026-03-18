package org.example.telecom_operations_dashboard.service;

import org.example.telecom_operations_dashboard.dto.cell.CellDetailsDto;
import org.example.telecom_operations_dashboard.dto.cell.GridCellDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CellService {
    CellDetailsDto getCellDetails(Integer cellId);
    List<GridCellDto> getAllGridCells();
    Page<GridCellDto> getAllGridCells(Pageable pageable);
}
