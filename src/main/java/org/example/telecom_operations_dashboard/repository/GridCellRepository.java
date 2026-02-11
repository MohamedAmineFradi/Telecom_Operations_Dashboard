package org.example.telecom_operations_dashboard.repository;

import org.example.telecom_operations_dashboard.model.GridCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GridCellRepository extends JpaRepository<GridCell, Integer> {

    @Query("SELECT g FROM GridCell g ORDER BY g.cellId ASC")
    List<GridCell> findAllOrderByCellId();
}
