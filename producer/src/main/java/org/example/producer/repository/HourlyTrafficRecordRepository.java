package org.example.producer.repository;

import org.example.producer.model.HourlyTrafficRecord;
import org.example.producer.model.HourlyTrafficRecordId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface HourlyTrafficRecordRepository extends JpaRepository<HourlyTrafficRecord, HourlyTrafficRecordId> {

    List<HourlyTrafficRecord> findByHourGreaterThanEqualAndHourLessThanOrderByHourAscCellIdAsc(
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable
    );
}
