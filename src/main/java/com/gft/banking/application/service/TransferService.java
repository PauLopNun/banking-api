package com.gft.banking.application.service;

import com.gft.banking.api.exception.ResourceNotFoundException;
import com.gft.banking.domain.model.Account;
import com.gft.banking.domain.model.Transfer;
import com.gft.banking.domain.model.User;
import com.gft.banking.infrastructure.persistence.AccountRepository;
import com.gft.banking.infrastructure.persistence.TransferRepository;
import com.gft.banking.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;

    @Transactional
    public Transfer transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String username) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("No puedes transferir a tu propia cuenta");
        }

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Account fromAccount = accountRepository.findByIdAndOwner(fromAccountId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta origen no encontrada"));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta destino no encontrada"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return transferRepository.save(Transfer.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<Transfer> getAccountHistory(Long accountId, Pageable pageable, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        accountRepository.findByIdAndOwner(accountId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + accountId));

        return transferRepository.findAccountHistory(accountId, pageable);
    }
}