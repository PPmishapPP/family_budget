package ru.mishapp.api.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.Media;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.entity.User;
import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.enumiration.Type;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.MediaRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;
import ru.mishapp.services.CurrentUserService;

import java.time.LocalDate;
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
    @Mock
    private PeriodicChangeRuleRepository periodicChangeRuleRepository;
    @Mock
    private MediaRepository mediaRepository;
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

    @Test
    void requireRuleReturnsRuleOfOwnedAccount() {
        PeriodicChangeRule rule = rule(7L, 10L);
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));
        when(periodicChangeRuleRepository.findByIdAndTargetAccountId(7L, 10L))
                .thenReturn(Optional.of(rule));

        assertSame(rule, service.requireRule(10L, 7L));
    }

    @Test
    void requireRuleThrowsWhenRuleBelongsToAnotherAccount() {
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));
        when(periodicChangeRuleRepository.findByIdAndTargetAccountId(7L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.requireRule(10L, 7L));
    }

    @Test
    void requireMediaReturnsMediaOfOwnedAccount() {
        Media media = media(15L, 10L);
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));
        when(mediaRepository.findByIdAndAccountId(15L, 10L)).thenReturn(Optional.of(media));

        assertSame(media, service.requireMedia(10L, 15L));
    }

    @Test
    void requireMediaThrowsWhenMediaBelongsToAnotherAccount() {
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(account));
        when(mediaRepository.findByIdAndAccountId(15L, 10L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.requireMedia(10L, 15L));
    }

    private PeriodicChangeRule rule(Long id, Long accountId) {
        return PeriodicChangeRule.builder()
                .id(id)
                .targetAccountId(accountId)
                .name("Интернет")
                .sum(-500)
                .type(Type.MONTHLY)
                .pass(0)
                .nextDay(LocalDate.now().plusDays(1))
                .active(true)
                .build();
    }

    private Media media(Long id, Long accountId) {
        return new Media(id, "Фильм", MediaStatus.PLAN_TO_WATCH, MediaType.MOVIE,
                accountId, 8, "Описание");
    }
}
