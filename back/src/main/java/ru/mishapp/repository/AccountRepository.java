package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.dto.AccountNames;

import java.util.List;
import java.util.Optional;


public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByNameAndChatId(String name, long chatId);
    
    @Query("select id, name from account")
    List<AccountNames> findAllNames();
}
