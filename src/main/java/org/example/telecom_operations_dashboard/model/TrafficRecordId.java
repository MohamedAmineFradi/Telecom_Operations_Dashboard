package org.example.telecom_operations_dashboard.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class TrafficRecordId implements Serializable {
    private OffsetDateTime datetime;
    private Integer cellId;
    private Integer countrycode;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrafficRecordId that = (TrafficRecordId) o;
        return Objects.equals(datetime, that.datetime) && Objects.equals(cellId, that.cellId) && Objects.equals(countrycode, that.countrycode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetime, cellId, countrycode);
    }
}

