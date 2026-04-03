package org.telecom_operations_dashboard.cell.repository;

import org.telecom_operations_dashboard.cell.model.GridCell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GridCellRepository extends JpaRepository<GridCell, Integer> {

    @Query("SELECT g FROM GridCell g ORDER BY g.cellId ASC")
    List<GridCell> findAllOrderByCellId();

    @Query("SELECT g FROM GridCell g ORDER BY g.cellId ASC")
    Page<GridCell> findAllOrderByCellId(Pageable pageable);
}
