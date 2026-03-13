package com.gft.banking.api.controller;

import com.gft.banking.api.dto.AccountDTO;
import com.gft.banking.application.service.AccountService;
import com.gft.banking.domain.model.Account;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(
            @Valid @RequestBody AccountDTO.CreateAccountRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Account account = accountService.createAccount(
                request.getOwnerName(),
                request.getInitialBalance(),
                userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<AccountDTO> accounts = accountService.getAllAccounts(userDetails.getUsername())
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(toDTO(
                accountService.getAccountById(id, userDetails.getUsername())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        accountService.deleteAccount(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    private AccountDTO toDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .ownerName(account.getOwnerName())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .build();
    }
}