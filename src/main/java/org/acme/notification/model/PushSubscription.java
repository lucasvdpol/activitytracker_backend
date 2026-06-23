package org.acme.notification.model;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.acme.user.User;

@Entity
@Table(name = "push_subscriptions")
public class PushSubscription extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String fcmToken;

    @ManyToOne
    public User user;

    public Instant createdAt = Instant.now();

    public static PushSubscription findByToken(String token) {
        return find("fcmToken", token).firstResult();
    }
}
