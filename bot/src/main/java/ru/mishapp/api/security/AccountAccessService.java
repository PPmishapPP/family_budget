package ru.mishapp.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.Account;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.services.CurrentUserService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountAccessService {

    private final CurrentUserService currentUserService;
    private final AccountRepository accountRepository;

    public Account requireAccount(Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, currentUserId())
                .orElseThrow(() -> new NoSuchElementException("Счёт не найден"));
    }

    private Long currentUserId() {
        return currentUserService.getCurrentUser()
                .map(user -> user.getId())
                .orElseThrow(() -> new InsufficientAuthenticationException("Пользователь не аутентифицирован"));
    }
}
