package com.gft.banking.api.controller;

import com.gft.banking.api.dto.AuthDTO;
import com.gft.banking.api.dto.RefreshTokenDTO;
import com.gft.banking.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthService.AuthLoginResponse> register(
            @Valid @RequestBody AuthDTO.AuthRequest request) {
        return ResponseEntity.ok(authService.register(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthService.AuthLoginResponse> login(
            @Valid @RequestBody AuthDTO.AuthRequest request) {
        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthService.AuthLoginResponse> refresh(
            @Valid @RequestBody RefreshTokenDTO.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }
}