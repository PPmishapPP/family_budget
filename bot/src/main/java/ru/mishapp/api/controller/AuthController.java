package ru.mishapp.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.dto.LoginRequest;
import ru.mishapp.api.dto.RefreshTokenRequest;
import ru.mishapp.api.dto.TokenResponse;
import ru.mishapp.api.dto.UserDto;
import ru.mishapp.api.security.AuthCookieService;
import ru.mishapp.api.security.AuthService;

@RestController
@RequestMapping(ApiPaths.V1 + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.issueRefreshCookie(tokens.refreshToken(), httpRequest))
                .body(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false) String cookieToken,
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletRequest httpRequest) {
        TokenResponse tokens = authService.refresh(
                cookieService.resolveRefreshToken(cookieToken, body));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.issueRefreshCookie(tokens.refreshToken(), httpRequest))
                .body(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false) String cookieToken,
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletRequest httpRequest) {
        authService.logout(cookieService.resolveRefreshToken(cookieToken, body));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        cookieService.clearRefreshCookie(httpRequest))
                .build();
    }

    @GetMapping("/me")
    public UserDto me() {
        return authService.currentUser();
    }
}
