package com.gft.banking.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferDTO {

    private Long id;
    private Long fromAccountId;
    private String fromAccountOwner;
    private Long toAccountId;
    private String toAccountOwner;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    @Data
    public static class TransferRequest {

        @NotNull(message = "La cuenta origen es obligatoria")
        private Long fromAccountId;

        @NotNull(message = "La cuenta destino es obligatoria")
        private Long toAccountId;

        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01", message = "El importe debe ser mayor que cero")
        private BigDecimal amount;
    }
}