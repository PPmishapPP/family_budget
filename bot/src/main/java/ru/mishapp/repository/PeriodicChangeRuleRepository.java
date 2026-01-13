package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.PeriodicChangeRule;

import java.time.LocalDate;
import java.util.List;

public interface PeriodicChangeRuleRepository extends CrudRepository<PeriodicChangeRule, Long> {
	List<PeriodicChangeRule> findByActiveTrueAndNextDayLessThanEqual(LocalDate targetDate);
}
