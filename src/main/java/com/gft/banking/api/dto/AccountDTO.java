package com.gft.banking.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountDTO {

    private Long id;
    private String ownerName;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    // Request para crear cuenta
    @Data
    public static class CreateAccountRequest {

        @NotBlank(message = "El nombre del titular es obligatorio")
        private String ownerName;

        @NotNull(message = "El saldo inicial es obligatorio")
        @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
        private BigDecimal initialBalance;
    }
}