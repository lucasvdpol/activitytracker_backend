package org.acme.groups.model;

import java.time.LocalDateTime;

import org.acme.user.User;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "group_members", uniqueConstraints = @UniqueConstraint(columnNames = { "group_id", "user_id" }))
public class GroupMember extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "group_id")
    public Group group;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    public LocalDateTime joinedAt;
}
