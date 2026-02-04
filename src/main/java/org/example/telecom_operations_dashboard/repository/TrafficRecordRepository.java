package org.example.telecom_operations_dashboard.repository;

import org.example.telecom_operations_dashboard.model.TrafficRecord;
import org.example.telecom_operations_dashboard.model.TrafficRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TrafficRecordRepository extends JpaRepository<TrafficRecord, TrafficRecordId> {

    @Query("""
        SELECT t
        FROM TrafficRecord t
        WHERE t.datetime BETWEEN :start AND :end
          AND t.cellId = :cellId
        ORDER BY t.datetime ASC
        """)
    List<TrafficRecord> findByCellAndInterval(
            @Param("cellId") Integer cellId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    Optional<TrafficRecord> findFirstByCellIdOrderByDatetimeDesc(Integer cellId);

    @Query("SELECT COUNT(DISTINCT t.cellId) FROM TrafficRecord t")
    long countDistinctCells();

    @Query("SELECT MAX(t.datetime) FROM TrafficRecord t")
    OffsetDateTime findLatestDatetime();
}
