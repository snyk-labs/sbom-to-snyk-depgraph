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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
//@Setter NO SETTER HERE!
@NoArgsConstructor
@ToString
@Entity
@Table(name = "event", schema = Const.SCHEMA_NAME)
public class TestEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotNull
    @Column(name = "mess_body", nullable = false, length = 250)
    private String message;

    @Setter
    @NotNull
    @Column(name = "time_mark", nullable = false)
    private Date timeMark;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "eventId")
    @ToString.Exclude
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
}
