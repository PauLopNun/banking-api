package com.gft.banking.application.service;

import com.gft.banking.domain.model.RefreshToken;
import com.gft.banking.domain.model.User;
import com.gft.banking.infrastructure.persistence.UserRepository;
import com.gft.banking.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthLoginResponse register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateToken(username);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);

        return new AuthLoginResponse(accessToken, refreshToken.getToken());
    }

    public AuthLoginResponse login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        String accessToken = jwtService.generateToken(username);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);

        return new AuthLoginResponse(accessToken, refreshToken.getToken());
    }

    public AuthLoginResponse refresh(String token) {
        RefreshToken rotated = refreshTokenService.rotateRefreshToken(token);
        String accessToken = jwtService.generateToken(rotated.getUser().getUsername());
        return new AuthLoginResponse(accessToken, rotated.getToken());
    }

    public record AuthLoginResponse(String accessToken, String refreshToken) {}
}