package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.mishapp.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    @Query("SELECT * FROM app_user WHERE login = :login")
    Optional<User> findByLogin(@Param("login") String login);

    @Query("""
            SELECT u.*
            FROM app_user u
            JOIN user_account ua ON ua.user_id = u.id
            WHERE ua.account_id = :accountId""")
    List<User> findAllByAccountId(@Param("accountId") Long accountId);
}
