package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

@Table("account")
@Getter
public class Account {
    @Id
    private final Long id;
    @Nonnull
    private final String name;
    private final boolean status;
    private final long chatId;
    @MappedCollection(idColumn = "account_id")
    private final Set<AccountHistory> history;

    public Account(String name, boolean status, long chatId) {
        this(null, name, status, chatId, new HashSet<>());
    }

    @PersistenceCreator
    public Account(Long id, @Nonnull String name, boolean status, long chatId, Set<AccountHistory> history) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.chatId = chatId;
        this.history = history;
    }
}
