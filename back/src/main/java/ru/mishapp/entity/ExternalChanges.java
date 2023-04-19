package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;


@Table("external_change")
@Getter
public class ExternalChanges {
    @Id
    private final long id;
    private final int sum;
    @Nonnull
    private final String name;
    @Nonnull
    private final Type type;
    private final int pass;
    
    @MappedCollection(idColumn = "external_change_id")
    private final List<AccountHistory> history;
    
    @PersistenceCreator
    public ExternalChanges(long id, int sum, @Nonnull String name, @Nonnull Type type,
                           int pass, @Nonnull LocalDate startDay, boolean status, List<AccountHistory> history
    ) {
        this.id = id;
        this.sum = sum;
        this.name = name;
        this.type = type;
        this.pass = pass;
        this.history = history;
    }
    
    public enum Type {
        ONE_TIME,
        MONTHLY,
        WEEKLY,
        DAILY
    }
}
