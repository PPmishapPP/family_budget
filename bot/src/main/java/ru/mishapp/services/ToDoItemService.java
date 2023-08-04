package ru.mishapp.services;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.ToDoItem;
import ru.mishapp.repository.ToDoItemRepository;

@Service
@RequiredArgsConstructor
public class ToDoItemService {
    
    private final ToDoItemRepository repository;
    public ToDoItem save(ToDoItem toDoItem) {
        return repository.save(toDoItem);
    }
    
    public List<ToDoItem> readAllByChatId(Long chatId) {
        return repository.readAllByChatId(chatId);
    }
    
    public ToDoItem delete(long id) {
        Optional<ToDoItem> byId = repository.findById(id);
        if (byId.isEmpty()) {
            throw new IllegalArgumentException("Нет дела с таким ID");
        }
        
        repository.delete(byId.get());
        return byId.get();
    }
}
