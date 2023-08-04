package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@Table("user")
@Getter
public class User {
    @Id
    private final Long id;
    private final long chatId;
    @Nonnull
    private final String name;
    
    @PersistenceCreator
    public User(Long id, long chatId, @Nonnull String name) {
        this.id = id;
        this.chatId = chatId;
        this.name = name;
    }
}
