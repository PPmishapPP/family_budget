package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.services.records.HistoryPage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AccountHistoryRepository accountHistoryRepository;

    @Transactional(readOnly = true)
    public HistoryPage page(long accountId, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть отрицательным");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Размер страницы должен быть от 1 до " + MAX_PAGE_SIZE);
        }

        long total = accountHistoryRepository.countByAccountId(accountId);
        List<AccountHistory> content = accountHistoryRepository
                .findHistoryPageByAccountId(accountId, size, (long) page * size);
        int totalPages = (int) ((total + size - 1) / size);
        return new HistoryPage(content, page, size, total, totalPages);
    }
}
