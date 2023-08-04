package ru.mishapp.schedulers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.IBot;
import ru.mishapp.entity.ToDoItem;
import ru.mishapp.repository.ToDoItemRepository;

@Service
@RequiredArgsConstructor
public class ToDoScheduler {
    
    private final ToDoItemRepository repository;
    private final IBot bot;
    
    @Scheduled(cron = "${schedule.todo-reminder}")
    @Transactional
    public void execute() {
        Map<Long, List<ToDoItem>> map = StreamSupport.stream(repository.findAll().spliterator(), false)
            .collect(Collectors.groupingBy(ToDoItem::getChatId));
        
        for (Map.Entry<Long, List<ToDoItem>> entry : map.entrySet()) {
            Long chatId = entry.getKey();
            List<ToDoItem> items = entry.getValue();
            StringBuilder builder = new StringBuilder();
            for (ToDoItem item : items) {
                builder.append(item.getId());
                builder.append(") ");
                builder.append(item.toTelegram());
            }
            bot.sendMessage(builder.toString(), chatId);
        }
    }
}
