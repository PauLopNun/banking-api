package com.gft.banking.api.controller;

import com.gft.banking.api.dto.AuthDTO;
import com.gft.banking.application.service.AccountService;
import com.gft.banking.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthDTO.AuthResponse> register(@Valid @RequestBody AuthDTO.AuthRequest request) {
        String token = authService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new AuthDTO.AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.AuthResponse> login(@Valid @RequestBody AuthDTO.AuthRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new AuthDTO.AuthResponse(token));
    }
}