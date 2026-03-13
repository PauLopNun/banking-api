package com.gft.banking.application.service;

import com.gft.banking.domain.model.RefreshToken;
import com.gft.banking.domain.model.User;
import com.gft.banking.infrastructure.persistence.RefreshTokenRepository;
import com.gft.banking.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("dev")
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User mockUser() {
        return User.builder()
                .id(1L)
                .username("pau")
                .password("hashed")
                .role(User.Role.USER)
                .build();
    }

    @Test
    void shouldCreateRefreshTokenSuccessfully() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 604800000L);
        User user = mockUser();

        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken("pau");

        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getExpiresAt()).isAfter(Instant.now());
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void shouldRotateRefreshTokenSuccessfully() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 604800000L);
        User user = mockUser();
        RefreshToken existing = RefreshToken.builder()
                .token("old-token")
                .user(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.rotateRefreshToken("old-token");

        assertThat(result.getToken()).isNotEqualTo("old-token");
        assertThat(result.getUser()).isEqualTo(user);
        verify(refreshTokenRepository).delete(existing);
    }

    @Test
    void shouldThrowWhenRefreshTokenNotFound() {
        when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no válido");
    }

    @Test
    void shouldThrowWhenRefreshTokenExpired() {
        User user = mockUser();
        RefreshToken expired = RefreshToken.builder()
                .token("expired-token")
                .user(user)
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("expired-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirado");
    }
}