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
        left join account as ac ON h.account_id = ac.id
        where ac.name = :accountName
          and h.date_time = (select max(date_time)
                             from account_history
                                      join account as a ON h.account_id = ac.id
                             where a.name = :accountName
                             group by a.name)""")
    AccountHistory findLastByAccountName(@Param("accountName") String accountName);
}
