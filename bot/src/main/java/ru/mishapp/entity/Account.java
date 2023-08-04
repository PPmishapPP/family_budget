package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("account")
@Getter
public class Account {
    @Id
    private final Long id;
    @Nonnull
    private final String name;
    private final boolean status;
    private final long chatId;
    @MappedCollection(idColumn = "account_id", keyColumn = "date_time")
    private final List<AccountHistory> history;
    
    public Account(String name, boolean status, long chatId) {
        this(null, name, status, chatId, new ArrayList<>());
    }
    
    @PersistenceCreator
    public Account(Long id, @Nonnull String name, boolean status, long chatId, List<AccountHistory> history) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.chatId = chatId;
        this.history = history;
    }
    
    public String toTelegram() {
        StringBuilder message = new StringBuilder(String.format(
            "Id: %d, Имя: %s(%d₽)%n%n", id, name, history.get(history.size() - 1).getBalance()));
        
        for (AccountHistory accountHistory : history) {
            message.append(accountHistory.toTelegram());
        }
        
        return message.toString();
    }
}
