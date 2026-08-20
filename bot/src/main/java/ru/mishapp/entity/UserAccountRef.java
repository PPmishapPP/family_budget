package ru.mishapp.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("user_account")
@Getter
public class UserAccountRef {

    @Id
    private final Long id;
    @Column(value = "account_id")
    private final AggregateReference<Account, Long> account;

    public UserAccountRef(AggregateReference<Account, Long> account) {
        this(null, account);
    }

    @PersistenceCreator
    public UserAccountRef(Long id, AggregateReference<Account, Long> account) {
        this.id = id;
        this.account = Objects.requireNonNull(account, "account");
    }
}
