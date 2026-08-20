package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.mishapp.entity.AccountHistory;

public interface AccountHistoryRepository extends CrudRepository<AccountHistory, Long> {

    @Query("""
            select *
            from account_history as h
            where h.id = (select max(id)
                                 from account_history ah
                                 where ah.account_id = :accountId
                                 group by ah.account_id)""")
    AccountHistory findLastByAccountId(@Param("accountId") Long accountId);
}
