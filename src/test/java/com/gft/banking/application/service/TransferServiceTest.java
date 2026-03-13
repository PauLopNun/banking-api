package com.gft.banking.application.service;

import com.gft.banking.domain.model.Account;
import com.gft.banking.domain.model.Transfer;
import com.gft.banking.domain.model.User;
import com.gft.banking.infrastructure.persistence.AccountRepository;
import com.gft.banking.infrastructure.persistence.TransferRepository;
import com.gft.banking.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("dev")
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransferService transferService;

    private Account fromAccount;
    private Account toAccount;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("pau")
                .password("hashed")
                .role(User.Role.USER)
                .build();

        fromAccount = Account.builder()
                .id(1L)
                .ownerName("Pau López")
                .balance(new BigDecimal("1000"))
                .owner(user)
                .build();

        toAccount = Account.builder()
                .id(2L)
                .ownerName("Ivan Carmona")
                .balance(new BigDecimal("500"))
                .owner(User.builder().id(2L).username("ivan").password("hashed").role(User.Role.USER).build())
                .build();
    }

    @Test
    void shouldTransferSuccessfully() {
        // Arrange
        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Transfer result = transferService.transfer(1L, 2L, new BigDecimal("200"), "pau");

        // Assert
        assertThat(fromAccount.getBalance()).isEqualByComparingTo("800");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("700");
        verify(accountRepository, times(2)).save(any());
        verify(transferRepository).save(any());
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.ZERO, "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("-100"), "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    void shouldThrowWhenSameAccount() {
        assertThatThrownBy(() -> transferService.transfer(1L, 1L, new BigDecimal("100"), "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("propia cuenta");
    }

    @Test
    void shouldThrowWhenInsufficientBalance() {
        // Arrange
        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        // Act & Assert
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("9999"), "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    @Test
    void shouldThrowWhenFromAccountNotFound() {
        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndOwner(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(99L, 2L, new BigDecimal("100"), "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("origen no encontrada");
    }

    @Test
    void shouldThrowWhenToAccountNotFound() {
        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(1L, 99L, new BigDecimal("100"), "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("destino no encontrada");
    }

    @Test
    void shouldThrowWhenFromAccountBelongsToAnotherUser() {
        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("100"), "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("origen no encontrada");
    }
}