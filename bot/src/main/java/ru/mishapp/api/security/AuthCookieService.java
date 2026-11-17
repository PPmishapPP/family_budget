package ru.mishapp.api.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.config.JwtProperties;
import ru.mishapp.api.dto.RefreshTokenRequest;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthCookieService {

    public static final String REFRESH_COOKIE = "refresh_token";
    private static final String REFRESH_COOKIE_PATH = ApiPaths.V1 + "/auth";

    private final JwtProperties jwtProperties;

    public String issueRefreshCookie(String token, HttpServletRequest request) {
        return cookie(token, jwtProperties.refreshTtl(), request).toString();
    }

    public String clearRefreshCookie(HttpServletRequest request) {
        return cookie("", Duration.ZERO, request).toString();
    }

    public String resolveRefreshToken(String cookieToken, RefreshTokenRequest body) {
        if (cookieToken != null && !cookieToken.isBlank()) {
            return cookieToken;
        }
        return body == null ? null : body.refreshToken();
    }

    private ResponseCookie cookie(String token, Duration maxAge, HttpServletRequest request) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Strict")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
