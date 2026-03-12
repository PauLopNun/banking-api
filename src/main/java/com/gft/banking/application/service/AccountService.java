package com.gft.banking.application.service;

import com.gft.banking.domain.model.Account;
import com.gft.banking.infrastructure.persistence.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(String ownerName, BigDecimal initialBalance) {
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }
        if (accountRepository.existsByOwnerName(ownerName)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese titular");
        }

        Account account = Account.builder()
                .ownerName(ownerName)
                .balance(initialBalance)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con id: " + id));
    }

    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con id: " + id));

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("No puedes eliminar una cuenta con saldo");
        }

        accountRepository.deleteById(id);
    }
}