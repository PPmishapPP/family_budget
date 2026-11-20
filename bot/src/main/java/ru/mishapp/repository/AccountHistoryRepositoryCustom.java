package ru.mishapp.repository;

import ru.mishapp.entity.AccountHistory;

import java.util.List;

public interface AccountHistoryRepositoryCustom {

    List<AccountHistory> findHistoryPage(
            long accountId,
            int limit,
            long offset,
            String sortColumn,
            boolean ascending
    );
}
