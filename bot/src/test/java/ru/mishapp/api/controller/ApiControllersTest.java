package ru.mishapp.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import ru.mishapp.api.config.CorsProperties;
import ru.mishapp.api.config.JwtConfiguration;
import ru.mishapp.api.dto.MediaDto;
import ru.mishapp.api.config.JwtProperties;
import ru.mishapp.api.config.OpenApiConfiguration;
import ru.mishapp.api.mapper.AccountApiMapper;
import ru.mishapp.api.mapper.ForecastApiMapper;
import ru.mishapp.api.mapper.MediaApiMapper;
import ru.mishapp.api.mapper.PaymentRuleApiMapper;
import ru.mishapp.api.mapper.ReferenceApiMapper;
import ru.mishapp.api.security.AccountAccessService;
import ru.mishapp.api.security.ApiSecurityConfiguration;
import ru.mishapp.api.security.AuthCookieService;
import ru.mishapp.api.security.JwtService;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.Media;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.entity.User;
import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.PaymentDirection;
import ru.mishapp.enumiration.Type;
import ru.mishapp.services.AccountService;
import ru.mishapp.services.ForecastService;
import ru.mishapp.services.HistoryService;
import ru.mishapp.services.MediaService;
import ru.mishapp.services.PeriodicChangeRuleService;
import ru.mishapp.services.RuleExecuteService;
import ru.mishapp.services.records.CalcItem;
import ru.mishapp.services.records.HistoryPage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AuthController.class,
        AccountController.class,
        HistoryController.class,
        PaymentController.class,
        PaymentRuleController.class,
        ForecastController.class,
        MediaController.class,
        ReferenceController.class
})
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ResourceServerWebSecurityAutoConfiguration.class,
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class
})
@Import({JwtConfiguration.class, JwtService.class, AuthCookieService.class, ApiSecurityConfiguration.class, OpenApiConfiguration.class,
        AccountApiMapper.class, PaymentRuleApiMapper.class, MediaApiMapper.class, ForecastApiMapper.class,
        ReferenceApiMapper.class})
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=01234567890123456789012345678901",
        "app.security.jwt.access-ttl=15m",
        "app.security.jwt.refresh-ttl=30d",
        "app.cors.allowed-origins=http://localhost:5173"
})
class ApiControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private ru.mishapp.api.security.AuthService authService;

    @MockitoBean
    private AccountAccessService accessService;

    @MockitoBean
    private HistoryService historyService;

    @MockitoBean
    private PeriodicChangeRuleService periodicChangeRuleService;

    @MockitoBean
    private RuleExecuteService ruleExecuteService;

    @MockitoBean
    private ForecastService forecastService;

    @MockitoBean
    private MediaService mediaService;

    private String authorization;

    @BeforeEach
    void setUp() {
        String token = jwtService.issueAccessToken(new User(1L, "user", "hash", "UTC", Set.of()));
        authorization = "Bearer " + token;
    }

    @Test
    void shouldExposeOpenApiDocument() throws Exception {
        String document = mockMvc.perform(get("/v3/api-docs").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Family Budget API"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/accounts/{accountId}/media']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Files.createDirectories(Path.of("build"));
        Files.writeString(Path.of("build/openapi.json"), document);
    }

    @Test
    void shouldListAccounts() throws Exception {
        when(accountService.readAllAccounts()).thenReturn(List.of(
                new Account(10L, "Семья", true, Set.of())));

        mockMvc.perform(get("/api/v1/accounts").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Семья"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldRejectApiWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Требуется авторизация"));
    }

    @Test
    void shouldReturnBalance() throws Exception {
        when(accessService.requireAccount(10L))
                .thenReturn(new Account(10L, "Семья", true, Set.of()));
        when(accountService.getBalance(10L)).thenReturn(1500);

        mockMvc.perform(get("/api/v1/accounts/10/balance").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.balance").value(1500));
    }

    @Test
    void shouldPageHistory() throws Exception {
        when(accessService.requireAccount(10L))
                .thenReturn(new Account(10L, "Семья", true, Set.of()));
        when(historyService.page(10L, 1, 2)).thenReturn(new HistoryPage(
                List.of(new AccountHistory(
                        5L, 10L, -100, 1400, LocalDateTime.of(2026, 11, 16, 12, 0), "Продукты")),
                1, 2, 3, 2));

        mockMvc.perform(get("/api/v1/accounts/10/history?page=1&size=2")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].comment").value("Продукты"));
    }

    @Test
    void shouldRegisterExpenseAsNegativeSum() throws Exception {
        when(accessService.requireAccount(10L))
                .thenReturn(new Account(10L, "Семья", true, Set.of()));
        when(accountService.registerPayment(10L, "Продукты", 100, PaymentDirection.EXPENSE))
                .thenReturn(new AccountHistory(
                        5L, 10L, -100, 1400, LocalDateTime.now(), "Продукты"));

        mockMvc.perform(post("/api/v1/accounts/10/payments")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Продукты","amount":100,"direction":"EXPENSE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sum").value(-100))
                .andExpect(jsonPath("$.balance").value(1400));
    }

    @Test
    void shouldCreatePaymentRule() throws Exception {
        when(periodicChangeRuleService.create(eq(10L), any()))
                .thenReturn(PeriodicChangeRule.builder()
                        .id(7L)
                        .targetAccountId(10L)
                        .name("Интернет")
                        .sum(-500)
                        .type(Type.MONTHLY)
                        .pass(0)
                        .nextDay(LocalDate.of(2026, 12, 1))
                        .active(true)
                        .build());

        mockMvc.perform(post("/api/v1/accounts/10/payment-rules")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Интернет","sum":-500,"type":"MONTHLY","nextDay":"2026-12-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/v1/accounts/10/payment-rules/7"))
                .andExpect(jsonPath("$.name").value("Интернет"))
                .andExpect(jsonPath("$.type").value("MONTHLY"));
    }

    @Test
    void shouldExecutePaymentRule() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/10/payment-rules/7/execute")
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());

        verify(ruleExecuteService).execute(10L, 7L);
    }

    @Test
    void shouldUpdatePaymentRule() throws Exception {
        when(periodicChangeRuleService.update(eq(10L), eq(7L), any()))
                .thenReturn(PeriodicChangeRule.builder()
                        .id(7L)
                        .targetAccountId(10L)
                        .name("Интернет")
                        .sum(-600)
                        .type(Type.MONTHLY)
                        .pass(2)
                        .nextDay(LocalDate.of(2026, 12, 1))
                        .active(true)
                        .endDate(LocalDate.of(2027, 12, 1))
                        .build());

        mockMvc.perform(put("/api/v1/accounts/10/payment-rules/7")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Интернет","sum":-600,"type":"MONTHLY",
                                 "nextDay":"2026-12-01","endDate":"2027-12-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(-600))
                .andExpect(jsonPath("$.pass").value(2));
    }

    @Test
    void shouldCalculateForecastFromSavedRules() throws Exception {
        LocalDate to = LocalDate.of(2026, 12, 1);
        when(accessService.requireAccount(10L))
                .thenReturn(new Account(10L, "Семья", true, Set.of()));
        when(forecastService.calculateForecast(eq(10L), eq(to), any())).thenReturn(List.of(
                new CalcItem(to, 1400, new PeriodicChangeRuleDto(
                        7L, "Интернет", -500, "MONTHLY", 0, to, true, null))));

        mockMvc.perform(post("/api/v1/accounts/10/forecast")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"to":"2026-12-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-12-01"))
                .andExpect(jsonPath("$[0].operation").value("Интернет"))
                .andExpect(jsonPath("$[0].sum").value(-500))
                .andExpect(jsonPath("$[0].balance").value(1400));
    }

    @Test
    void shouldFilterMediaByType() throws Exception {
        when(accessService.requireAccount(10L))
                .thenReturn(new Account(10L, "Семья", true, Set.of()));
        when(mediaService.findAll(10L, ru.mishapp.enumiration.MediaType.MOVIE)).thenReturn(List.of(
                new Media(8L, "Фильм", MediaStatus.WATCHED, ru.mishapp.enumiration.MediaType.MOVIE, 10L, 8, "Хороший")));

        mockMvc.perform(get("/api/v1/accounts/10/media?type=MOVIE")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Фильм"))
                .andExpect(jsonPath("$[0].type").value("MOVIE"))
                .andExpect(jsonPath("$[0].status").value("WATCHED"));
    }

    @Test
    void shouldCreateMedia() throws Exception {
        when(accessService.requireAccount(10L))
                .thenReturn(new Account(10L, "Семья", true, Set.of()));
        when(mediaService.create(eq(10L), any())).thenReturn(
                new Media(8L, "Фильм", MediaStatus.PLAN_TO_WATCH, ru.mishapp.enumiration.MediaType.MOVIE, 10L, null, null));

        mockMvc.perform(post("/api/v1/accounts/10/media")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Фильм","type":"MOVIE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/accounts/10/media/8"))
                .andExpect(jsonPath("$.status").value("PLAN_TO_WATCH"));
    }

    @Test
    void shouldReturnReferenceData() throws Exception {
        mockMvc.perform(get("/api/v1/reference/media-statuses")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("PLAN_TO_WATCH"))
                .andExpect(jsonPath("$[0].label").value("Ожидает просмотра"));
    }
}
