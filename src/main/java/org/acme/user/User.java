package org.acme.user;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    public String name;

    @Column(unique = true, nullable = false)
    public String email;

    public String password;

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
