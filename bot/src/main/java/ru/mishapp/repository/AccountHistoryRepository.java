package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.mishapp.entity.AccountHistory;

public interface AccountHistoryRepository extends CrudRepository<AccountHistory, Long> {
    
    @Query("""
        select *
        from account_history as h
        where account_id = :accountId and h.date_time = (select max(date_time) from account_history where account_id = :accountId group by account_id)""")
    AccountHistory findLast(@Param("accountId") Long accountId);
    
    @Query("""
        select *
        from account_history as h
        left join account a on h.account_id = a.id
        where a.chat_id = :chatId and h.date_time = (select max(date_time) from account_history group by account_id)""")
    AccountHistory findLastByChatId(@Param("chatId") Long chatId);
}
