package org.acme.auth;

import org.acme.user.User;

public record UserResponse(Long id, String name, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id, user.name, user.email);
    }
}
