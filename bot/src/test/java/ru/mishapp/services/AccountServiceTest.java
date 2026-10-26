package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountHistoryRepository accountHistoryRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Test
    void shouldAddIncomeToBalance() {
        long accountId = 1L;
        when(accountHistoryRepository.findLastByAccountId(accountId))
                .thenReturn(history(accountId, 0, 1000));
        when(accountHistoryRepository.save(any(AccountHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountHistory payment = accountService.registerPayment(accountId, "Зарплата", 500);

        assertEquals(500, payment.getSum());
        assertEquals(1500, payment.getBalance());
        assertEquals("Зарплата", payment.getComment());
        assertNotNull(payment.getDateTime());
        verify(accountHistoryRepository).save(payment);
    }

    @Test
    void shouldSubtractExpenseFromBalance() {
        long accountId = 1L;
        when(accountHistoryRepository.findLastByAccountId(accountId))
                .thenReturn(history(accountId, 0, 1000));
        when(accountHistoryRepository.save(any(AccountHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountHistory payment = accountService.registerPayment(accountId, "Продукты", -300);

        assertEquals(-300, payment.getSum());
        assertEquals(700, payment.getBalance());
        assertEquals("Продукты", payment.getComment());
    }

    @Test
    void shouldStartFromZeroWhenHistoryIsEmpty() {
        long accountId = 1L;
        when(accountHistoryRepository.findLastByAccountId(accountId)).thenReturn(null);
        when(accountHistoryRepository.save(any(AccountHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountHistory payment = accountService.registerPayment(accountId, "Подарок", 250);

        assertEquals(250, payment.getBalance());
    }

    @Test
    void shouldTrimPaymentName() {
        long accountId = 1L;
        when(accountHistoryRepository.findLastByAccountId(accountId))
                .thenReturn(history(accountId, 0, 0));
        when(accountHistoryRepository.save(any(AccountHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountHistory payment = accountService.registerPayment(accountId, "  Продукты  ", -100);

        assertEquals("Продукты", payment.getComment());
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.registerPayment(1L, "  ", -100));
    }

    @Test
    void shouldRejectZeroSum() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.registerPayment(1L, "Продукты", 0));
    }

    private AccountHistory history(long accountId, int sum, int balance) {
        return new AccountHistory(
                1L, accountId, sum, balance, LocalDateTime.now().minusDays(1), "Инициализация");
    }
}
