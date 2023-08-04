package ru.mishapp.entity;


import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("periodic_change")
@Getter
public class PeriodicChange {
    @Id
    private final Long id;
    @Nonnull
    private final String name;
    private final long chatId;
    @MappedCollection(idColumn = "periodic_change_id")
    private final Set<PeriodicChangeRule> rules;
    
    @PersistenceCreator
    public PeriodicChange(Long id, @Nonnull String name, long chatId, Set<PeriodicChangeRule> rules) {
        this.id = id;
        this.name = name;
        this.chatId = chatId;
        this.rules = rules;
    }
    
    public PeriodicChange(@Nonnull String name, long chatId) {
        this(null, name, chatId, new HashSet<>());
    }
    
    public String toTelegram() {
        StringBuilder builder = new StringBuilder(name + "\n");
        for (PeriodicChangeRule rule : rules) {
            builder.append("    ");
            builder.append(rule.toTelegram());
            builder.append("\n");
        }
        return builder.toString();
    }
}
