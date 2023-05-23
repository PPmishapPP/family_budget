package ru.mishapp.entity;


import java.time.LocalDate;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import ru.mishapp.Constants;

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
        return String.format("%s(%s₽, %s)", name, Constants.RUB.format(sum), type.description);
    }
    
    
    public enum Type {
        MONTHLY("Ежемесячно", LocalDate::plusMonths),
        WEEKLY("Еженедельно", LocalDate::plusWeeks),
        DAILY("Ежедневно", LocalDate::plusDays);
        
        private final String description;
        private final BiFunction<LocalDate, Integer, LocalDate> next;
        
        
        Type(String description, BiFunction<LocalDate, Integer, LocalDate> next) {
            this.description = description;
            this.next = next;
        }
        
        public static Optional<Type> of(String type) {
            for (Type value : values()) {
                if (value.description.equals(type)) {
                    return Optional.of(value);
                }
            }
            return Optional.empty();
        }
        
        public LocalDate next(LocalDate day, int pass) {
            return next.apply(day, pass + 1);
        }
    }
}
