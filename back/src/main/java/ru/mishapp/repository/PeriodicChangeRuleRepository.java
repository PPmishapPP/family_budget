package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.PeriodicChangeRule;

public interface PeriodicChangeRuleRepository extends CrudRepository<PeriodicChangeRule, Long> {
}
