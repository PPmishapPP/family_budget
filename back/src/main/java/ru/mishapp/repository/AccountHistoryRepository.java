package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.mishapp.entity.AccountHistory;

public interface AccountHistoryRepository extends CrudRepository<AccountHistory, Long> {
    
    @Query("""
        select *
        from account_history as h
        where h.date_time = (select max(date_time) from account_history where account_id = :account_id group by account_id)""")
    AccountHistory findLast(@Param("account_id") Long accountId);
}
