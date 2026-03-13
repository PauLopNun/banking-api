package com.gft.banking.api.controller;

import com.gft.banking.api.dto.TransferDTO;
import com.gft.banking.application.service.TransferService;
import com.gft.banking.domain.model.Transfer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferDTO> transfer(
            @Valid @RequestBody TransferDTO.TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Transfer transfer = transferService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                userDetails.getUsername()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(transfer));
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<Page<TransferDTO>> getHistory(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<TransferDTO> history = transferService.getAccountHistory(accountId, pageable, userDetails.getUsername())
                .map(this::toDTO);

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