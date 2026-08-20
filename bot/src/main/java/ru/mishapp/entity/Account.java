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
    @MappedCollection(idColumn = "account_id")
    private final Set<AccountHistory> history;

    public Account(String name, boolean status) {
        this(null, name, status, new HashSet<>());
    }

    @PersistenceCreator
    public Account(Long id, @Nonnull String name, boolean status, Set<AccountHistory> history) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.history = history;
    }
}
