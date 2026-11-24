package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.services.records.HistoryPage;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "comment", "comment",
            "sum", "sum",
            "date", "date_time",
            "balance", "balance"
    );

    private final AccountHistoryRepository accountHistoryRepository;

    @Transactional(readOnly = true)
    public HistoryPage page(long accountId, int page, int size, String sort, String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть отрицательным");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Размер страницы должен быть от 1 до " + MAX_PAGE_SIZE);
        }
        if (sort == null) {
            throw new IllegalArgumentException("Поле сортировки не задано");
        }
        String sortColumn = SORT_COLUMNS.get(sort);
        if (sortColumn == null) {
            throw new IllegalArgumentException("Неизвестное поле сортировки: " + sort);
        }
        if (direction == null) {
            throw new IllegalArgumentException("Направление сортировки не задано");
        }
        String normalizedDirection = direction.toLowerCase(Locale.ROOT);
        boolean ascending;
        if ("asc".equals(normalizedDirection)) {
            ascending = true;
        } else if ("desc".equals(normalizedDirection)) {
            ascending = false;
        } else {
            throw new IllegalArgumentException("Неизвестное направление сортировки: " + direction);
        }

        long total = accountHistoryRepository.countByAccountId(accountId);
        List<AccountHistory> content = accountHistoryRepository
                .findHistoryPage(accountId, size, (long) page * size, sortColumn, ascending);
        int totalPages = (int) ((total + size - 1) / size);
        return new HistoryPage(content, page, size, total, totalPages);
    }
}
