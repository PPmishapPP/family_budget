package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@Table("todo")
@Getter
public class ToDoItem {
    @Id
    private final Long id;
    @Nonnull
    private final String description;
    @Nonnull
    private final Long chatId;
    
    @PersistenceCreator
    public ToDoItem(Long id, @Nonnull String description, @Nonnull Long chatId) {
        this.id = id;
        this.description = description;
        this.chatId = chatId;
    }
    
    public ToDoItem(@Nonnull String description, Long chatId) {
        this(null, description, chatId);
    }
    
    public String toTelegram() {
        return description;
    }
}
