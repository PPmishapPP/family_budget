package ru.mishapp.entity;

import lombok.Getter;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("duty_user")
public class DutyUser {
    @Column("user_id")
    @Getter
    private final AggregateReference<User, Long> id;
    
    @PersistenceCreator
    public DutyUser(AggregateReference<User, Long> id) {
        this.id = id;
    }
}
