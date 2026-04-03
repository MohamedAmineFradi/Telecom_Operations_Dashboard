package org.example.producer.repository;

import org.example.producer.model.MobilityRecord;
import org.example.producer.model.MobilityRecordId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MobilityRecordRepository extends JpaRepository<MobilityRecord, MobilityRecordId> {

    List<MobilityRecord> findByDatetimeGreaterThanEqualAndDatetimeLessThanOrderByDatetimeAscCellIdAscProvinciaAsc(
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable
    );
}