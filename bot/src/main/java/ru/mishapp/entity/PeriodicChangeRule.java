package ru.mishapp.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import ru.mishapp.Constants;
import ru.mishapp.enumiration.Type;

import java.time.LocalDate;

@Table("periodic_change_rule")
@Getter
@Builder
@With
public class PeriodicChangeRule {
    
    @Id
    private final Long id;
    private final long periodicChangeId;
    private final long targetAccountId;
    private final Long receivingAccountId;
    private final String name;
    private final int sum;
    private final Type type;
    private final int pass;
    private final LocalDate nextDay;
    
    @PersistenceCreator
    public PeriodicChangeRule(
        Long id,
        long periodicChangeId,
        long targetAccountId,
        Long receivingAccountId,
        String name, int sum,
        Type type,
        int pass,
        LocalDate nextDay
    ) {
        this.id = id;
        this.periodicChangeId = periodicChangeId;
        this.targetAccountId = targetAccountId;
        this.receivingAccountId = receivingAccountId;
        this.name = name;
        this.sum = sum;
        this.type = type;
        this.pass = pass;
        this.nextDay = nextDay;
    }
    
    public String toTelegram() {
        return String.format("%s: %s₽", name, Constants.RUB.format(sum));
    }
}
