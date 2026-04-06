package org.example.producer.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class HourlyTrafficRecordId implements Serializable {

    private OffsetDateTime hour;
    private Integer cellId;

    public HourlyTrafficRecordId() {}

    public HourlyTrafficRecordId(OffsetDateTime hour, Integer cellId) {
        this.hour = hour;
        this.cellId = cellId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HourlyTrafficRecordId that)) return false;
        return Objects.equals(hour, that.hour) &&
                Objects.equals(cellId, that.cellId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, cellId);
    }
}
