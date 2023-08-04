package ru.mishapp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.PeriodicChange;

public interface PeriodicChangeRepository extends CrudRepository<PeriodicChange, Long> {
    
    Optional<PeriodicChange> findByNameAndChatId(String name, long chatId);
    
    List<PeriodicChange> findAllByChatId(Long chatId);
}
