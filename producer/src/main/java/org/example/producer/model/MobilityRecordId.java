package org.example.producer.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

public class MobilityRecordId implements Serializable {

    private OffsetDateTime datetime;
    private Integer cellId;
    private String provincia;

    public MobilityRecordId() {}

    public MobilityRecordId(OffsetDateTime datetime, Integer cellId, String provincia) {
        this.datetime = datetime;
        this.cellId = cellId;
        this.provincia = provincia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MobilityRecordId that)) return false;
        return Objects.equals(datetime, that.datetime) &&
                Objects.equals(cellId, that.cellId) &&
                Objects.equals(provincia, that.provincia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datetime, cellId, provincia);
    }
}