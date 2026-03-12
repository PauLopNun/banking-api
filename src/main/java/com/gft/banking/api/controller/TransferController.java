package com.gft.banking.api.controller;

import com.gft.banking.api.dto.TransferDTO;
import com.gft.banking.application.service.TransferService;
import com.gft.banking.domain.model.Transfer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferDTO> transfer(@Valid @RequestBody TransferDTO.TransferRequest request) {
        Transfer transfer = transferService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(transfer));
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransferDTO>> getHistory(@PathVariable Long accountId) {
        List<TransferDTO> history = transferService.getAccountHistory(accountId)
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(history);
    }

    private TransferDTO toDTO(Transfer transfer) {
        return TransferDTO.builder()
                .id(transfer.getId())
                .fromAccountId(transfer.getFromAccount().getId())
                .fromAccountOwner(transfer.getFromAccount().getOwnerName())
                .toAccountId(transfer.getToAccount().getId())
                .toAccountOwner(transfer.getToAccount().getOwnerName())
                .amount(transfer.getAmount())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}