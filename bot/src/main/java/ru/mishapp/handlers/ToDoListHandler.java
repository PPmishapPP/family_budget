package ru.mishapp.handlers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.entity.ToDoItem;
import ru.mishapp.services.ToDoItemService;

@TelegramHandler("дела")
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ToDoListHandler {
    
    private final ToDoItemService toDoListService;
    
    @TelegramCommand()
    public String readAll(Long chatId) {
        List<ToDoItem> items = toDoListService.readAllByChatId(chatId);
        if (items.isEmpty()) {
            return "Нет дел";
        }
        
        StringBuilder builder = new StringBuilder();
        for (ToDoItem item : items) {
            builder.append(item.getId());
            builder.append(") ");
            builder.append(item.toTelegram());
        }
        return builder.toString();
    }
    
    @TelegramCommand("добавить")
    public String add(
        @TelegramParam("дело") String description,
        Long chatId
    ) {
        ToDoItem item = toDoListService.save(new ToDoItem(description, chatId));
        return "Дело добавлено: " + item.toTelegram();
    }
    
    @TelegramCommand("сделано")
    public String remove(
        @TelegramParam("дело") String itemNumber,
        Long chatId
    ) {
        ToDoItem item = toDoListService.delete(Long.parseLong(itemNumber));
        return "Дело сделано: " + item.toTelegram();
    }
}
