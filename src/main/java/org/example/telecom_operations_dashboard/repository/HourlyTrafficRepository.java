package org.example.telecom_operations_dashboard.repository;

import org.example.telecom_operations_dashboard.model.HourlyTrafficView;
import org.example.telecom_operations_dashboard.model.TrafficRecord;
import org.example.telecom_operations_dashboard.model.TrafficRecordId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface HourlyTrafficRepository extends Repository<TrafficRecord, TrafficRecordId> {

    @Query(value = """
        SELECT CAST(hour AS timestamptz) AS hour, cell_id AS cellId, total_activity AS totalActivity
        FROM v_hourly_traffic
        WHERE hour = :hour
        ORDER BY total_activity DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<HourlyTrafficView> findTopCellsAtHour(
            @Param("hour") OffsetDateTime hour,
            @Param("limit") int limit
    );


    @Query(value = """
        SELECT hour,
               cell_id      AS cellId,
               total_activity AS totalActivity
        FROM v_hourly_traffic
        WHERE hour = :hour
        """, nativeQuery = true)
    List<HourlyTrafficView> findAllAtHour(@Param("hour") OffsetDateTime hour);



}

