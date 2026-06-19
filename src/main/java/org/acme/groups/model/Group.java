package org.acme.groups.model;

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
@Table(name = "groups")
public class Group extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @ManyToOne
    @JoinColumn(name = "host_id")
    public User host;

    @Column(unique = true, nullable = false)
    public String joinCode;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<GroupMember> members;

}
