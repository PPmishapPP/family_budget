package ru.mishapp.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.api.dto.LoginRequest;
import ru.mishapp.api.dto.TokenResponse;
import ru.mishapp.api.dto.UserDto;
import ru.mishapp.entity.RefreshToken;
import ru.mishapp.entity.User;
import ru.mishapp.repository.RefreshTokenRepository;
import ru.mishapp.repository.UserRepository;
import ru.mishapp.services.CurrentUserService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.login())
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Неверный логин или пароль");
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        String rawToken = requireRefreshToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Refresh-токен недействителен"));
        if (stored.isRevoked() || !stored.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh-токен недействителен");
        }
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));
        refreshTokenRepository.save(stored.revoke());
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .ifPresent(token -> refreshTokenRepository.save(token.revoke()));
    }

    @Transactional(readOnly = true)
    public UserDto currentUser() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new InsufficientAuthenticationException(
                        "Пользователь не аутентифицирован"));
        return new UserDto(user.getId(), user.getLogin());
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.issueAccessToken(user);
        String refreshToken = generateRefreshToken();
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.save(new RefreshToken(
                user.getId(),
                hash(refreshToken),
                now.plus(jwtService.refreshTtl()),
                false,
                now
        ));
        return new TokenResponse(accessToken, refreshToken, "Bearer", jwtService.accessTtlSeconds());
    }

    private String requireRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh-токен отсутствует");
        }
        return refreshToken;
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 недоступен", exception);
        }
    }
}
