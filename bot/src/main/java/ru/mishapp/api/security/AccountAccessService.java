package ru.mishapp.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.Media;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.MediaRepository;
import ru.mishapp.repository.PeriodicChangeRuleRepository;
import ru.mishapp.services.CurrentUserService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountAccessService {

    private final CurrentUserService currentUserService;
    private final AccountRepository accountRepository;
    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;
    private final MediaRepository mediaRepository;

    public Account requireAccount(Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, currentUserId())
                .orElseThrow(() -> new NoSuchElementException("Счёт не найден"));
    }

    public PeriodicChangeRule requireRule(Long accountId, Long ruleId) {
        Account account = requireAccount(accountId);
        return periodicChangeRuleRepository.findByIdAndTargetAccountId(ruleId, account.getId())
                .orElseThrow(() -> new NoSuchElementException("Правило не найдено"));
    }

    public Media requireMedia(Long accountId, Long mediaId) {
        Account account = requireAccount(accountId);
        return mediaRepository.findByIdAndAccountId(mediaId, account.getId())
                .orElseThrow(() -> new NoSuchElementException("Медиа не найдено"));
    }

    private Long currentUserId() {
        return currentUserService.getCurrentUser()
                .map(user -> user.getId())
                .orElseThrow(() -> new InsufficientAuthenticationException("Пользователь не аутентифицирован"));
    }
}
