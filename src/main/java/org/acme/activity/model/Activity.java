package org.acme.activity.model;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.user.User;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="activities")
public class Activity extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    public String location;

    public String description;

    public LocalDateTime startTime;

    public LocalDateTime endTime;

    public Integer maxParticipants;

    @ManyToOne
    @JoinColumn(name = "host_id")
    public User host;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ActivityParticipant> participants;

}
