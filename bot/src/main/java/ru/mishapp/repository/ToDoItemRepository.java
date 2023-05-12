package ru.mishapp.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.ToDoItem;

public interface ToDoItemRepository extends CrudRepository<ToDoItem, Long> {
    
    List<ToDoItem> readAllByChatId(Long chatId);
}
