package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.mishapp.entity.Account;

import java.util.List;
import java.util.Optional;


public interface AccountRepository extends CrudRepository<Account, Long> {

    @Query("""
            SELECT a.*
            FROM account a
            JOIN user_account ua ON ua.account_id = a.id
            WHERE ua.user_id = :userId""")
    List<Account> findAllByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT a.*
            FROM account a
            JOIN user_account ua ON ua.account_id = a.id
            WHERE a.id = :accountId
              AND ua.user_id = :userId""")
    Optional<Account> findByIdAndUserId(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId
    );

}
