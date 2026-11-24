package ru.mishapp.api.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuthService authService;

    private final User user = new User(1L, "user", "$2a$10$hash", "UTC", Set.of());

    @Test
    void shouldLoginAndStoreOnlyTokenHash() {
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", user.getPassword())).thenReturn(true);
        when(jwtService.issueAccessToken(user)).thenReturn("access-token");
        when(jwtService.accessTtlSeconds()).thenReturn(900L);
        when(jwtService.refreshTtl()).thenReturn(Duration.ofDays(30));

        TokenResponse response = authService.login(new LoginRequest("user", "secret"));

        assertEquals("access-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900, response.expiresIn());
        assertNotNull(response.refreshToken());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertEquals(1L, saved.getUserId().longValue());
        assertFalse(saved.isRevoked());
        assertEquals(64, saved.getTokenHash().length());
        assertNotEquals(response.refreshToken(), saved.getTokenHash());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now().plusDays(29)));
    }

    @Test
    void shouldRejectUnknownLogin() {
        when(userRepository.findByLogin("nobody")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("nobody", "secret")));

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void shouldRejectWrongPassword() {
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("user", "wrong")));

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void shouldRotateRefreshToken() {
        String rawToken = "old-refresh-token";
        RefreshToken stored = new RefreshToken(
                10L, 1L, hash(rawToken),
                LocalDateTime.now().plusDays(1), false, LocalDateTime.now().minusDays(1)
        );
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(stored));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtService.issueAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.accessTtlSeconds()).thenReturn(900L);
        when(jwtService.refreshTtl()).thenReturn(Duration.ofDays(30));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TokenResponse response = authService.refresh(rawToken);

        assertEquals("new-access-token", response.accessToken());
        assertNotEquals(rawToken, response.refreshToken());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        List<RefreshToken> saved = captor.getAllValues();

        assertTrue(saved.get(0).isRevoked());
        assertEquals(stored.getTokenHash(), saved.get(0).getTokenHash());
        assertFalse(saved.get(1).isRevoked());
        assertEquals(64, saved.get(1).getTokenHash().length());
        assertNotEquals(hash(rawToken), saved.get(1).getTokenHash());
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        String rawToken = "expired-token";
        RefreshToken stored = new RefreshToken(
                10L, 1L, hash(rawToken),
                LocalDateTime.now().minusMinutes(1), false, LocalDateTime.now().minusDays(31)
        );
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(stored));

        assertThrows(BadCredentialsException.class, () -> authService.refresh(rawToken));

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectRevokedRefreshToken() {
        String rawToken = "revoked-token";
        RefreshToken stored = new RefreshToken(
                10L, 1L, hash(rawToken),
                LocalDateTime.now().plusDays(1), true, LocalDateTime.now().minusDays(1)
        );
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(stored));

        assertThrows(BadCredentialsException.class, () -> authService.refresh(rawToken));

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectMissingRefreshToken() {
        assertThrows(IllegalArgumentException.class, () -> authService.refresh("  "));

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void shouldRevokeTokenOnLogout() {
        String rawToken = "logout-token";
        RefreshToken stored = new RefreshToken(
                10L, 1L, hash(rawToken),
                LocalDateTime.now().plusDays(1), false, LocalDateTime.now().minusDays(1)
        );
        when(refreshTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.logout(rawToken);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertTrue(captor.getValue().isRevoked());
    }

    @Test
    void shouldIgnoreLogoutWithoutToken() {
        authService.logout(null);

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void shouldReturnCurrentUser() {
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(user));

        assertEquals(new UserDto(1L, "user"), authService.currentUser());
    }

    @Test
    void shouldRejectCurrentUserWhenNotAuthenticated() {
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());

        assertThrows(InsufficientAuthenticationException.class, () -> authService.currentUser());
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
