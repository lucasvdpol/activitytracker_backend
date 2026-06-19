package org.acme.activity.model;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.user.User;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne
    @JoinColumn(name = "host_id")
    public User host;

    @ManyToMany
    @JoinTable(
        name = "activity_invited_users",
        joinColumns = @JoinColumn(name = "activity_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public List<User> invitedUsers;

}
