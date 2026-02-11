package org.example.telecom_operations_dashboard.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class MobilityRecordId implements Serializable {
    private OffsetDateTime datetime;
    private Integer cellId;
    private String provincia;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MobilityRecordId that = (MobilityRecordId) o;
        return Objects.equals(datetime, that.datetime)
                && Objects.equals(cellId, that.cellId)
                && Objects.equals(provincia, that.provincia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetime, cellId, provincia);
    }
}
