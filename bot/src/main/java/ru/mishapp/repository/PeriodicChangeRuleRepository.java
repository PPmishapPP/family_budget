package ru.mishapp.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.mishapp.dto.PeriodicChangeRuleDto;
import ru.mishapp.entity.PeriodicChangeRule;

import java.util.List;

public interface PeriodicChangeRuleRepository extends CrudRepository<PeriodicChangeRule, Long> {

    @Query("""
            SELECT
                r.id as id,
                r.name as name,
                r.target_account_id as target_account_id,
                r.sum as sum,
                r.type as type,
                r.pass as pass,
                r.next_day as next_day,
                r.active as active,
                r.end_date as end_date
            FROM periodic_change_rule r
            WHERE r.target_account_id = :accountId""")
    List<PeriodicChangeRuleDto> findAllRuleDtos(@Param("accountId") long accountId);
}
