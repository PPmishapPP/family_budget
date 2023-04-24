package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.PeriodicChange;

import java.util.Optional;

public interface PeriodicChangeRepository extends CrudRepository<PeriodicChange, Long> {
    
    Optional<PeriodicChange> findByNameAndChatId(String name, long chatId);
}
