package ru.mishapp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.mishapp.dto.AccountBalance;
import ru.mishapp.dto.AccountNames;
import ru.mishapp.entity.Account;



public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByNameAndChatId(String name, long chatId);
    
    @Query("select id, name from account")
    List<AccountNames> findAllNames();
    
    @Query("""
        select account.id, account.name, ah.balance from account
        left join account_history ah on account.id = ah.account_id
        and ah.date_time = (select max(date_time) from account_history as ah2 where ah2.account_id = account.id)
        where account.chat_id = :chatId""")
    List<AccountBalance> findAllNamesByChatId(@Param("chatId") long chatId);
}
