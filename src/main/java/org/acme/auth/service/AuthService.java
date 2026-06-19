package org.acme.auth.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.acme.auth.dto.AuthResponse;
import org.acme.auth.dto.LoginRequest;
import org.acme.auth.dto.RegisterRequest;
import org.acme.auth.dto.UserResponse;
import org.acme.user.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Inject
    AuthService authService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (User.findByEmail(request.email()) != null) {
            throw new WebApplicationException("Email is already in use", Response.Status.CONFLICT);
        }

        User user = new User();
        user.name = request.name();
        user.email = request.email();
        user.password = BcryptUtil.bcryptHash(request.password());
        user.persist();

        return UserResponse.from(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = User.findByEmail(request.email());
        if (user == null || !BcryptUtil.matches(request.password(), user.password)) {
            throw new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED);
        }

        String token = Jwt.issuer(issuer)
                .upn(user.email)
                .groups("user")
                .claim("name", user.name)
                .expiresIn(Duration.ofHours(24))
                .sign();

        return new AuthResponse(token);
    }
}
