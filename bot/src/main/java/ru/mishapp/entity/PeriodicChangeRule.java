package ru.mishapp.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import ru.mishapp.enumiration.Type;

import java.time.LocalDate;

@Table("periodic_change_rule")
@Getter
@Builder
@With
public class PeriodicChangeRule {

    @Id
    private final Long id;
    private final long targetAccountId;
    private final String name;
    private final int sum;
    private final Type type;
    private final int pass;
    private final LocalDate nextDay;
    private final boolean active;
    private final LocalDate endDate;

    @PersistenceCreator
    public PeriodicChangeRule(
            Long id,
            long targetAccountId,
            String name, int sum,
            Type type,
            int pass,
            LocalDate nextDay,
            boolean active,
            LocalDate endDate
    ) {
        this.id = id;
        this.targetAccountId = targetAccountId;
        this.name = name;
        this.sum = sum;
        this.type = type;
        this.pass = pass;
        this.nextDay = nextDay;
        this.active = active;
        this.endDate = endDate;
    }
}
