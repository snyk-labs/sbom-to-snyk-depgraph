package com.mfvanek.hibernate.entities;

import com.mfvanek.hibernate.consts.Const;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
//@Setter NO SETTER HERE!
@Entity
@Table(name = "event", schema = Const.SCHEMA_NAME)
public class TestEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "mess_body", nullable = false, length = 250)
    private String message;
    @NotNull
    @Column(name = "time_mark", nullable = false)
    private Date timeMark;
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "eventId")
    private Set<TestEventInfo> info = new HashSet<>();

    public TestEvent(final String message, final Date timeMark) {
        this.message = message;
        this.timeMark = timeMark;
    }

    public void setInfo(final Set<TestEventInfo> info) {
        this.info = info;
    }

    public void addEventInfo(final Set<TestEventInfo> value) {
        value.forEach(item -> item.setEventId(this));
        info.addAll(value);
    }

    public Long getId() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }

    public Date getTimeMark() {
        return this.timeMark;
    }

    public Set<TestEventInfo> getInfo() {
        return this.info;
    }

    public TestEvent() {
    }

    @Override
    public String toString() {
        return "TestEvent(id=" + this.getId() + ", message=" + this.getMessage() + ", timeMark=" + this.getTimeMark() + ")";
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setTimeMark(final Date timeMark) {
        this.timeMark = timeMark;
    }
}
