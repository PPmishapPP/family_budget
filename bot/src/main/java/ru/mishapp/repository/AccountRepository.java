package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.Account;


public interface AccountRepository extends CrudRepository<Account, Long> {

}
