package com.gft.banking.application.service;

import com.gft.banking.api.exception.ResourceNotFoundException;
import com.gft.banking.domain.model.Account;
import com.gft.banking.domain.model.User;
import com.gft.banking.infrastructure.persistence.AccountRepository;
import com.gft.banking.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public Account createAccount(String ownerName, BigDecimal initialBalance, String username) {
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (accountRepository.existsByOwnerNameAndOwner(ownerName, owner)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese titular");
        }

        Account account = Account.builder()
                .ownerName(ownerName)
                .balance(initialBalance)
                .owner(owner)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts(String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return accountRepository.findByOwner(owner);
    }

    public Account getAccountById(Long id, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return accountRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));
    }

    public void deleteAccount(Long id, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Account account = accountRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id: " + id));

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("No puedes eliminar una cuenta con saldo");
        }

        accountRepository.deleteById(id);
    }
}