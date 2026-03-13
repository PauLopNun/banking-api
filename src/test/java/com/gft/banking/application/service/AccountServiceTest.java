package com.gft.banking.application.service;

import com.gft.banking.domain.model.Account;
import com.gft.banking.domain.model.User;
import com.gft.banking.infrastructure.persistence.AccountRepository;
import com.gft.banking.infrastructure.persistence.UserRepository;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User mockUser() {
        return User.builder()
                .id(1L)
                .username("pau")
                .password("hashed")
                .role(User.Role.USER)
                .build();
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        String ownerName = "Pau López";
        BigDecimal balance = new BigDecimal("1000");
        User user = mockUser();

        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.existsByOwnerNameAndOwner(ownerName, user)).thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Account result = accountService.createAccount(ownerName, balance, "pau");

        assertThat(result.getOwnerName()).isEqualTo(ownerName);
        assertThat(result.getBalance()).isEqualByComparingTo(balance);
        assertThat(result.getOwner()).isEqualTo(user);
        verify(accountRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsNegative() {
        assertThatThrownBy(() ->
                accountService.createAccount("Pau López", new BigDecimal("-100"), "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativo");
    }

    @Test
    void shouldThrowExceptionWhenOwnerAlreadyExists() {
        User user = mockUser();
        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.existsByOwnerNameAndOwner("Pau López", user)).thenReturn(true);

        assertThatThrownBy(() ->
                accountService.createAccount("Pau López", new BigDecimal("500"), "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundForUser() {
        User user = mockUser();
        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndOwner(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(99L, "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void shouldDeleteAccountSuccessfully() {
        User user = mockUser();
        Account emptyAccount = Account.builder()
                .id(1L)
                .ownerName("Pau López")
                .balance(BigDecimal.ZERO)
                .owner(user)
                .build();

        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(emptyAccount));

        assertThatCode(() -> accountService.deleteAccount(1L, "pau"))
                .doesNotThrowAnyException();
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingAccountWithBalance() {
        User user = mockUser();
        when(userRepository.findByUsername("pau")).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(
                Account.builder()
                        .id(1L)
                        .ownerName("Pau López")
                        .balance(new BigDecimal("500"))
                        .owner(user)
                        .build()
        ));

        assertThatThrownBy(() -> accountService.deleteAccount(1L, "pau"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("saldo");
    }
}