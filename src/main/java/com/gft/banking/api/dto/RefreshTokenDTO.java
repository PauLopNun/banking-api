package com.gft.banking.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class RefreshTokenDTO {

    @Data
    public static class RefreshRequest {
        @NotBlank(message = "El refresh token es obligatorio")
        private String refreshToken;
    }

    @Data
    public static class RefreshResponse {
        private final String accessToken;
        private final String refreshToken;
    }
}