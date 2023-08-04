package ru.mishapp.entity;


import jakarta.annotation.Nonnull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import ru.mishapp.Constants;
import ru.mishapp.dto.Change;
import static ru.mishapp.Constants.DAY_AND_TIME;


@Table("account_history")
@Getter
@Builder(toBuilder = true)
public class AccountHistory {
    @Id
    private final Long id;
    private final long accountId;
    private final int sum;
    private final int balance;
    @Nonnull
    private final LocalDateTime dateTime;
    private final String comment;
    
    @PersistenceCreator
    public AccountHistory(Long id, long accountId, int sum, int balance, @Nonnull LocalDateTime dateTime, String comment) {
        this.id = id;
        this.accountId = accountId;
        this.sum = sum;
        this.balance = balance;
        this.dateTime = dateTime;
        this.comment = comment;
    }
    
    public AccountHistory(long accountId, int sum, int balance, @Nonnull LocalDateTime dateTime, String comment) {
        this(null, accountId, sum, balance, dateTime, comment);
    }
    
    public AccountHistory applyChange(Change change) {
        return new AccountHistory(this.accountId, change.sum(), balance + change.sum(), LocalDateTime.now(), change.comment());
    }
    
    public String toTelegram() {
        return String.format("%s - %s: %s, Баланс: %s%n", dateTime.format(DAY_AND_TIME), comment, Constants.RUB.format(Math.abs(sum)), Constants.RUB.format(balance));
    }
}
