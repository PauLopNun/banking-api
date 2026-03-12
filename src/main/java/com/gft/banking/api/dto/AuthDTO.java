package com.gft.banking.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDTO {

    @Data
    public static class AuthRequest {
        @NotBlank(message = "El usuario es obligatorio")
        private String username;
        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private final String token;
    }
}