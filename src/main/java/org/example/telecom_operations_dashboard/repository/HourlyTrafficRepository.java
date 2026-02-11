package org.example.telecom_operations_dashboard.repository;

import org.example.telecom_operations_dashboard.model.GridCellView;
import org.example.telecom_operations_dashboard.model.HeatmapCellView;
import org.example.telecom_operations_dashboard.model.HourlyTrafficSummaryView;
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
        SELECT CAST(hour AS timestamptz) AS hour,
               cell_id AS cellId,
               total_smsin AS totalSmsin,
               total_smsout AS totalSmsout,
               total_callin AS totalCallin,
               total_callout AS totalCallout,
               total_internet AS totalInternet,
               total_activity AS totalActivity
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
             cell_id AS cellId,
             total_smsin AS totalSmsin,
             total_smsout AS totalSmsout,
             total_callin AS totalCallin,
             total_callout AS totalCallout,
             total_internet AS totalInternet,
             total_activity AS totalActivity
        FROM v_hourly_traffic
        WHERE hour = :hour
        """, nativeQuery = true)
    List<HourlyTrafficView> findAllAtHour(@Param("hour") OffsetDateTime hour);

    @Query(value = """
        SELECT CAST(hour AS timestamptz) AS hour,
               SUM(total_smsin) AS totalSmsin,
               SUM(total_smsout) AS totalSmsout,
               SUM(total_callin) AS totalCallin,
               SUM(total_callout) AS totalCallout,
               SUM(total_internet) AS totalInternet,
               SUM(total_activity) AS totalActivity
        FROM v_hourly_traffic
        WHERE hour = :hour
        GROUP BY hour
        """, nativeQuery = true)
    HourlyTrafficSummaryView findSummaryAtHour(@Param("hour") OffsetDateTime hour);

    @Query(value = """
        SELECT v.cell_id AS cellId,
             v.total_smsin AS totalSmsin,
             v.total_smsout AS totalSmsout,
             v.total_callin AS totalCallin,
             v.total_callout AS totalCallout,
             v.total_internet AS totalInternet,
             v.total_activity AS totalActivity,
               g.bounds AS bounds,
               ST_X(ST_Centroid(ST_Transform(g.geometry, 4326))) AS lon,
               ST_Y(ST_Centroid(ST_Transform(g.geometry, 4326))) AS lat
        FROM v_hourly_traffic v
        JOIN dim_grid_milan g ON g.cell_id = v.cell_id
        WHERE v.hour = :hour
        """, nativeQuery = true)
    List<HeatmapCellView> findHeatmapWithGeometry(@Param("hour") OffsetDateTime hour);

    @Query(value = """
        SELECT g.cell_id AS cellId,
               g.bounds AS bounds,
               ST_X(ST_Centroid(g.geometry)) AS centroidX,
               ST_Y(ST_Centroid(g.geometry)) AS centroidY
        FROM dim_grid_milan g
        WHERE g.cell_id = :cellId
        """, nativeQuery = true)
    GridCellView findGridCell(@Param("cellId") Integer cellId);



}

