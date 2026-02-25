package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.PeriodicChangeRule;

import java.time.LocalDate;
import java.util.List;

public interface PeriodicChangeRuleRepository extends CrudRepository<PeriodicChangeRule, Long> {
	List<PeriodicChangeRule> findByActiveTrueAndNextDayLessThanEqual(LocalDate targetDate);

	@Query("""
        SELECT
            r.id as id,
            r.name as name,
            p.name as periodic_change_name,
            a.name as target_account_name,
            r.sum as sum,
            r.type as type,
            r.pass as pass,
            r.next_day as next_day,
            r.active as active,
            r.end_date as end_date
        FROM periodic_change_rule r
        LEFT JOIN periodic_change p ON r.periodic_change_id = p.id
        LEFT JOIN account a ON r.target_account_id = a.id
        WHERE p.chat_id = :chatId
            """)
	List<PeriodicChangeRuleDto> findAllRuleDtos(long chatId);
}
