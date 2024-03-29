package com.mfvanek.hibernate.entities;

import com.mfvanek.hibernate.consts.Const;
import com.mfvanek.hibernate.enums.TestEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bson.types.ObjectId;
import java.util.Objects;
//@Setter NO SETTER HERE!
@Entity
@Table(name = "event_info", schema = Const.SCHEMA_NAME)
public class TestEventInfo {
    @Id
    @NotNull
    @Size(min = 24, max = 24)
    @Column(name = "id", columnDefinition = "varchar", length = 24)
    private String id;
    @NotNull
    @Column(name = "info_body", nullable = false)
    private String info;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "info_type", columnDefinition = "varchar", length = 50, nullable = false)
    private TestEventType infoType;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private TestEvent eventId;

    public TestEventInfo(final TestEventType infoType, final String info) {
        this(ObjectId.get(), infoType, info);
    }

    private TestEventInfo(final ObjectId id, final TestEventType infoType, final String info) {
        this.id = id.toHexString();
        this.infoType = infoType;
        this.info = info;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 41).append(id).append(info).append(infoType).toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof TestEventInfo other)) {
            return false;
        }
        return new EqualsBuilder().append(this.id, other.id).append(this.info, other.info).append(this.infoType, other.infoType).append(this.eventId, other.eventId).isEquals();
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void setEventId(final TestEvent event) {
        Objects.requireNonNull(event, "event can\'t be null");
        if (this.eventId != null && this.eventId != event) {
            throw new IllegalStateException("eventId is already set");
        }
        this.eventId = event;
    }

    public String getId() {
        return this.id;
    }

    public String getInfo() {
        return this.info;
    }

    public TestEventType getInfoType() {
        return this.infoType;
    }

    public TestEvent getEventId() {
        return this.eventId;
    }

    public TestEventInfo() {
    }

    @Override
    public String toString() {
        return "TestEventInfo(id=" + this.getId() + ", info=" + this.getInfo() + ", infoType=" + this.getInfoType() + ")";
    }

    public void setInfo(final String info) {
        this.info = info;
    }

    public void setInfoType(final TestEventType infoType) {
        this.infoType = infoType;
    }
}
