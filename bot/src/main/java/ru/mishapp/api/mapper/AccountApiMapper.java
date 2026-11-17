package ru.mishapp.api.mapper;

import org.springframework.stereotype.Component;
import ru.mishapp.api.dto.AccountDto;
import ru.mishapp.api.dto.AccountHistoryDto;
import ru.mishapp.api.dto.BalanceDto;
import ru.mishapp.api.dto.PageResponse;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.services.records.HistoryPage;

@Component
public class AccountApiMapper {

    public AccountDto toDto(Account account) {
        return new AccountDto(account.getId(), account.getName(), account.isStatus());
    }

    public AccountHistoryDto toDto(AccountHistory history) {
        return new AccountHistoryDto(
                history.getId(),
                history.getSum(),
                history.getBalance(),
                history.getDateTime(),
                history.getComment()
        );
    }

    public BalanceDto toBalanceDto(Long accountId, int balance) {
        return new BalanceDto(accountId, balance);
    }

    public PageResponse<AccountHistoryDto> toPageResponse(HistoryPage page) {
        return new PageResponse<>(
                page.content().stream().map(this::toDto).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
