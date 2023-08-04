package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.mishapp.entity.Duty;

import java.time.LocalDate;

public interface DutyRepository extends CrudRepository<Duty, Long> {
    
    @Modifying
    @Query("""
        update duty set last_messages = now(), last_user_id = :lastUser
        where id = :id""")
    void updateNext(
        @Param("id") long id,
        @Param("lastUser") Long lastUser
    );
    
}
