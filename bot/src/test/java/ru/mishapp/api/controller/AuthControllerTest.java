package ru.mishapp.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mishapp.api.config.CorsProperties;
import ru.mishapp.api.config.JwtConfiguration;
import ru.mishapp.api.config.JwtProperties;
import ru.mishapp.api.dto.TokenResponse;
import ru.mishapp.api.dto.UserDto;
import ru.mishapp.api.security.ApiSecurityConfiguration;
import ru.mishapp.api.security.AuthCookieService;
import ru.mishapp.api.security.AuthService;
import ru.mishapp.api.security.JwtService;
import ru.mishapp.entity.User;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ResourceServerWebSecurityAutoConfiguration.class
})
@Import({JwtConfiguration.class, JwtService.class, AuthCookieService.class, ApiSecurityConfiguration.class})
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=01234567890123456789012345678901",
        "app.security.jwt.access-ttl=15m",
        "app.security.jwt.refresh-ttl=30d",
        "app.cors.allowed-origins=http://localhost:5173"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldLoginAndSetHttpOnlyRefreshCookie() throws Exception {
        when(authService.login(any()))
                .thenReturn(new TokenResponse("access", "refresh-body", "Bearer", 900));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"user","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-body"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(cookie().value("refresh_token", "refresh-body"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("Path=/api/v1/auth"),
                                org.hamcrest.Matchers.containsString("SameSite=Strict")
                        )));
    }

    @Test
    void shouldRejectInvalidLoginBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ошибка валидации"));
    }

    @Test
    void shouldRefreshFromCookie() throws Exception {
        when(authService.refresh("cookie-token"))
                .thenReturn(new TokenResponse("new-access", "new-refresh", "Bearer", 900));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "cookie-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(cookie().value("refresh_token", "new-refresh"));
    }

    @Test
    void shouldRefreshFromBodyForAndroid() throws Exception {
        when(authService.refresh("body-token"))
                .thenReturn(new TokenResponse("new-access", "new-refresh", "Bearer", 900));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"body-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void shouldReturnBadRequestWithoutRefreshToken() throws Exception {
        when(authService.refresh(null))
                .thenThrow(new IllegalArgumentException("Refresh-токен отсутствует"));

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Некорректный запрос"));
    }

    @Test
    void shouldLogoutAndClearCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "cookie-token")))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0))
                .andExpect(cookie().value("refresh_token", ""));

        verify(authService).logout("cookie-token");
    }

    @Test
    void shouldRejectUnauthenticatedMe() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Требуется авторизация"));
    }

    @Test
    void shouldReturnCurrentUser() throws Exception {
        when(authService.currentUser()).thenReturn(new UserDto(1L, "user"));
        String accessToken = jwtService.issueAccessToken(
                new User(1L, "user", "hash", "UTC", Set.of()));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("user"));
    }
}
