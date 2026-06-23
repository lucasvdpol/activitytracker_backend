package org.acme.auth.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.acme.user.User;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String tokenHash;

    @ManyToOne
    public User user;

    public Instant expiresAt;

    public boolean revoked = false;

    public static RefreshToken findByTokenHash(String tokenHash) {
        return find("tokenHash", tokenHash).firstResult();
    }
}
