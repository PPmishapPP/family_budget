package ru.mishapp.entity;


import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@Table("periodic_change")
@Getter
public class PeriodicChange {
    @Id
    private final Long id;
    @Nonnull
    private final String name;
    private final long chatId;
    
    @PersistenceCreator
    public PeriodicChange(Long id, @Nonnull String name, long chatId) {
        this.id = id;
        this.name = name;
        this.chatId = chatId;
    }
    
    public PeriodicChange(@Nonnull String name, long chatId) {
        this(null, name, chatId);
    }
    
    public String toTelegram() {
        return name;
    }
}
