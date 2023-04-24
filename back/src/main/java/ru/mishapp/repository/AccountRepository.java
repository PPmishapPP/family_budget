package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.Account;

import java.util.Optional;


public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByNameAndChatId(String name, long chatId);
}
