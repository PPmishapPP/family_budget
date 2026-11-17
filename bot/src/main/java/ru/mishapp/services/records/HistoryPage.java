package ru.mishapp.services.records;

import ru.mishapp.entity.AccountHistory;

import java.util.List;

public record HistoryPage(
        List<AccountHistory> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
