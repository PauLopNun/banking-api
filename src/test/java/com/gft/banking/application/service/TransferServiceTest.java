package com.gft.banking.application.service;

import com.gft.banking.domain.model.Account;
import com.gft.banking.domain.model.Transfer;
import com.gft.banking.infrastructure.persistence.AccountRepository;
import com.gft.banking.infrastructure.persistence.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private TransferService transferService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = Account.builder()
                .id(1L)
                .ownerName("Pau López")
                .balance(new BigDecimal("1000"))
                .build();

        toAccount = Account.builder()
                .id(2L)
                .ownerName("Ivan Carmona")
                .balance(new BigDecimal("500"))
                .build();
    }

    @Test
    void shouldTransferSuccessfully() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Transfer result = transferService.transfer(1L, 2L, new BigDecimal("200"));

        // Assert
        assertThat(fromAccount.getBalance()).isEqualByComparingTo("800");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("700");
        verify(accountRepository, times(2)).save(any());
        verify(transferRepository).save(any());
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("-100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    void shouldThrowWhenSameAccount() {
        assertThatThrownBy(() -> transferService.transfer(1L, 1L, new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("propia cuenta");
    }

    @Test
    void shouldThrowWhenInsufficientBalance() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        // Act & Assert
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("9999")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    @Test
    void shouldThrowWhenFromAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(99L, 2L, new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("origen no encontrada");
    }

    @Test
    void shouldThrowWhenToAccountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(1L, 99L, new BigDecimal("100")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("destino no encontrada");
    }
}