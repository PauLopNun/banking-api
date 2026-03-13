package com.gft.banking.application.service;

import com.gft.banking.domain.model.RefreshToken;
import com.gft.banking.domain.model.User;
import com.gft.banking.infrastructure.persistence.RefreshTokenRepository;
import com.gft.banking.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String token) {
        RefreshToken existing = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token no válido"));

        if (existing.isExpired()) {
            refreshTokenRepository.delete(existing);
            throw new IllegalArgumentException("Refresh token expirado");
        }

        refreshTokenRepository.delete(existing);

        RefreshToken newToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(existing.getUser())
                .expiresAt(Instant.now().plusMillis(refreshExpiration))
                .build();

        return refreshTokenRepository.save(newToken);
    }

    @Transactional
    public void revokeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        refreshTokenRepository.deleteByUser(user);
    }
}