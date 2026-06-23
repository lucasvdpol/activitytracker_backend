package org.acme.auth.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.acme.auth.dto.AuthResponse;
import org.acme.auth.dto.LoginRequest;
import org.acme.auth.dto.RefreshTokenRequest;
import org.acme.auth.dto.RegisterRequest;
import org.acme.auth.dto.UserResponse;
import org.acme.auth.model.RefreshToken;
import org.acme.user.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@ApplicationScoped
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "app.refresh-token.expiration-days")
    int refreshTokenExpirationDays;

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

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = User.findByEmail(request.email());
        if (user == null || !BcryptUtil.matches(request.password(), user.password)) {
            throw new WebApplicationException("Invalid credentials", Response.Status.UNAUTHORIZED);
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = findActiveRefreshToken(request.refreshToken());

        String accessToken = generateAccessToken(refreshToken.user);

        return new AuthResponse(accessToken, request.refreshToken());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = findActiveRefreshToken(request.refreshToken());
        refreshToken.revoked = true;
    }

    private RefreshToken findActiveRefreshToken(String rawToken) {
        RefreshToken refreshToken = RefreshToken.findByTokenHash(hashToken(rawToken));
        if (refreshToken == null || refreshToken.revoked || refreshToken.expiresAt.isBefore(Instant.now())) {
            throw new WebApplicationException("Invalid refresh token", Response.Status.UNAUTHORIZED);
        }
        return refreshToken;
    }

    private String generateAccessToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.email)
                .groups("user")
                .claim("name", user.name)
                .expiresIn(Duration.ofHours(24))
                .sign();
    }

    private String generateRefreshToken(User user) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.tokenHash = hashToken(rawToken);
        refreshToken.user = user;
        refreshToken.expiresAt = Instant.now().plus(Duration.ofDays(refreshTokenExpirationDays));
        refreshToken.persist();

        return rawToken;
    }

    private static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
