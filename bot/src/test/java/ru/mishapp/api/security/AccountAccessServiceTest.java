package ru.mishapp.api.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.User;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.services.CurrentUserService;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAccessServiceTest {

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private AccountAccessService service;

    private final User user = new User(1L, "user", "password", "UTC", Set.of());
    private final Account account = new Account(10L, "Семья", true, Set.of());

    @Test
    void requireAccountReturnsOwnedAccount() {
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));

        assertSame(account, service.requireAccount(10L));
    }

    @Test
    void requireAccountThrowsWhenAccountIsNotOwned() {
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.requireAccount(10L));
    }

    @Test
    void requireAccountThrowsWhenUserIsNotAuthenticated() {
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());

        assertThrows(InsufficientAuthenticationException.class, () -> service.requireAccount(10L));
        verifyNoInteractions(accountRepository);
    }
}
