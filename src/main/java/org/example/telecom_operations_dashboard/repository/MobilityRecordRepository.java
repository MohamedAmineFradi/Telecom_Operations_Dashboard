package org.example.telecom_operations_dashboard.repository;

import org.example.telecom_operations_dashboard.model.MobilityFlowView;
import org.example.telecom_operations_dashboard.model.MobilityProvinceSummaryView;
import org.example.telecom_operations_dashboard.model.MobilityRecord;
import org.example.telecom_operations_dashboard.model.MobilityRecordId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface MobilityRecordRepository extends Repository<MobilityRecord, MobilityRecordId> {

    @Query(value = """
        SELECT cell_id AS cellId,
               provincia AS provincia,
               SUM(cell2province) AS cellToProvince,
               SUM(province2cell) AS provinceToCell
        FROM fact_mobility_provinces
        WHERE DATE_TRUNC('hour', datetime) = :hour
          AND (:cellId IS NULL OR cell_id = :cellId)
          AND (:provincia IS NULL OR provincia = :provincia)
        GROUP BY cell_id, provincia
        ORDER BY (SUM(cell2province) + SUM(province2cell)) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<MobilityFlowView> findFlowsAtHour(
            @Param("hour") OffsetDateTime hour,
            @Param("cellId") Integer cellId,
            @Param("provincia") String provincia,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT provincia AS provincia,
               SUM(cell2province) AS totalCellToProvince,
               SUM(province2cell) AS totalProvinceToCell
        FROM fact_mobility_provinces
        WHERE DATE_TRUNC('hour', datetime) = :hour
          AND (:provincia IS NULL OR provincia = :provincia)
        GROUP BY provincia
        ORDER BY (SUM(cell2province) + SUM(province2cell)) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<MobilityProvinceSummaryView> findProvinceSummariesAtHour(
            @Param("hour") OffsetDateTime hour,
            @Param("provincia") String provincia,
            @Param("limit") int limit
    );
}
