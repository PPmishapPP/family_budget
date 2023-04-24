package ru.mishapp.entity;


import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.Optional;

@Table("periodic_change_rule")
@Getter
@Builder
public class PeriodicChangeRule {
    
    @Id
    private final Long id;
    private final long periodicChangeId;
    private final long targetAccountId;
    private final Long receivingAccountId;
    private final int sum;
    private final Type type;
    private final int pass;
    private final LocalDate startDay;
    
    @PersistenceCreator
    public PeriodicChangeRule(
        Long id,
        long periodicChangeId,
        long targetAccountId,
        Long receivingAccountId,
        int sum,
        Type type,
        int pass,
        LocalDate startDay
    ) {
        this.id = id;
        this.periodicChangeId = periodicChangeId;
        this.targetAccountId = targetAccountId;
        this.receivingAccountId = receivingAccountId;
        this.sum = sum;
        this.type = type;
        this.pass = pass;
        this.startDay = startDay;
    }
    
    public String toTelegram() {
        return String.valueOf(sum);
    }
    
    public enum Type {
        
        ONE_TIME("Один раз"),
        MONTHLY("Ежемесячно"),
        WEEKLY("Еженедельно"),
        DAILY("Ежедневно");
        
        private final String type;
        
        
        Type(String type) {
            this.type = type;
        }
        
        public static Optional<Type> of(String type) {
            for (Type value : values()) {
                if (value.type.equals(type)) {
                    return Optional.of(value);
                }
            }
            return Optional.empty();
        }
    }
}
