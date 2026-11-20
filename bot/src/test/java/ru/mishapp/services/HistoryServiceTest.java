package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.services.records.HistoryPage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private AccountHistoryRepository accountHistoryRepository;

    @InjectMocks
    private HistoryService historyService;

    @Test
    void shouldMapDateSortToDateTimeColumn() {
        when(accountHistoryRepository.countByAccountId(1L)).thenReturn(45L);
        when(accountHistoryRepository.findHistoryPage(1L, 20, 40L, "date_time", false))
                .thenReturn(List.of());

        HistoryPage page = historyService.page(1L, 2, 20, "date", "desc");

        assertThat(page.page()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(45);
        assertThat(page.totalPages()).isEqualTo(3);
        verify(accountHistoryRepository).findHistoryPage(1L, 20, 40L, "date_time", false);
    }

    @Test
    void shouldRejectUnknownSortField() {
        assertThatThrownBy(() -> historyService.page(1L, 0, 20, "unknown", "desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неизвестное поле сортировки");
    }

    @Test
    void shouldRejectUnknownSortDirection() {
        assertThatThrownBy(() -> historyService.page(1L, 0, 20, "date", "sideways"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неизвестное направление сортировки");
    }

    @Test
    void shouldRejectTooLargePageSize() {
        assertThatThrownBy(() -> historyService.page(1L, 0, 101, "date", "desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Размер страницы");
    }
}
