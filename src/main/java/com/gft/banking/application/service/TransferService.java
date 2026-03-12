package com.gft.banking.application.service;

import com.gft.banking.domain.model.Account;
import com.gft.banking.domain.model.Transfer;
import com.gft.banking.infrastructure.persistence.AccountRepository;
import com.gft.banking.infrastructure.persistence.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    @Transactional
    public Transfer transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero");
        }

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("No puedes transferir a tu propia cuenta");
        }

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada"));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada"));

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
    public List<Transfer> getAccountHistory(Long accountId) {
        return transferRepository.findAccountHistory(accountId);
    }
}