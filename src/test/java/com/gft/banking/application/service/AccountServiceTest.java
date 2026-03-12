package com.gft.banking.application.service;

import com.gft.banking.domain.model.Account;
import com.gft.banking.infrastructure.persistence.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("dev")
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() {
        // Arrange
        String ownerName = "Pau López";
        BigDecimal balance = new BigDecimal("1000");
        when(accountRepository.existsByOwnerName(ownerName)).thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Account result = accountService.createAccount(ownerName, balance);

        // Assert
        assertThat(result.getOwnerName()).isEqualTo(ownerName);
        assertThat(result.getBalance()).isEqualByComparingTo(balance);
        verify(accountRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsNegative() {
        // Act & Assert
        assertThatThrownBy(() ->
                accountService.createAccount("Pau López", new BigDecimal("-100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativo");
    }

    @Test
    void shouldThrowExceptionWhenOwnerAlreadyExists() {
        // Arrange
        when(accountRepository.existsByOwnerName("Pau López")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() ->
                accountService.createAccount("Pau López", new BigDecimal("500")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        // Arrange
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> accountService.getAccountById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void shouldDeleteAccountSuccessfully() {
        // Arrange
        Account emptyAccount = Account.builder()
                .id(1L)
                .ownerName("Pau López")
                .balance(BigDecimal.ZERO)
                .build();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(emptyAccount));

        // Act & Assert
        assertThatCode(() -> accountService.deleteAccount(1L))
                .doesNotThrowAnyException();
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingAccountWithBalance() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(
                Account.builder()
                        .id(1L)
                        .ownerName("Pau López")
                        .balance(new BigDecimal("500"))
                        .build()
        ));

        // Act & Assert
        assertThatThrownBy(() -> accountService.deleteAccount(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("saldo");
    }
}