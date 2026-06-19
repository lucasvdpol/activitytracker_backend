package org.acme.activity.model;

import java.time.LocalDateTime;

import org.acme.user.User;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "activity_participants", uniqueConstraints = @UniqueConstraint(columnNames = { "activity_id", "user_id" }))
public class ActivityParticipant extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "activity_id")
    public Activity activity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @Enumerated(EnumType.STRING)
    public ParticipantStatus status;

    public LocalDateTime respondedAt;
}
