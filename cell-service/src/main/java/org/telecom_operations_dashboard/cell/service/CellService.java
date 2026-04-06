package org.telecom_operations_dashboard.cell.service;

import org.telecom_operations_dashboard.common.dto.cell.CellDetailsDto;
import org.telecom_operations_dashboard.common.dto.cell.GridCellDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CellService {
    CellDetailsDto getCellDetails(Integer cellId);
    List<GridCellDto> getAllGridCells();
    Page<GridCellDto> getAllGridCells(Pageable pageable);
}
