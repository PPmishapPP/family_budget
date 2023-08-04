package ru.mishapp.entity;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("duty_user")
public class DutyUser {
    @Column("user_id")
    AggregateReference<User, Long> id;
    
}
