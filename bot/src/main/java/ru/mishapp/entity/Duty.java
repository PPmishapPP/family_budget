package ru.mishapp.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Table("duty")
@Getter
public class Duty {
    
    @Id
    private final Long id;
    
    private final long chatId;
    
    private final String name;
    
    @MappedCollection(idColumn = "duty_id", keyColumn = "user_id")
    private final List<DutyUser> users;
    
    private final LocalDate lastMessages;
    private final Long lastUserId;
    
    @Column("duty_account")
    private final long dutyAccountId;
    
    private final int award;
    
    @PersistenceCreator
    public Duty(
        Long id,
        long chatId,
        String name,
        List<DutyUser> users,
        LocalDate lastMessages,
        Long lastUserId,
        long dutyAccountId,
        int award
    ) {
        this.id = id;
        this.chatId = chatId;
        this.name = name;
        this.users = users;
        this.lastMessages = lastMessages;
        this.lastUserId = lastUserId;
        this.dutyAccountId = dutyAccountId;
        this.award = award;
    }
    
    public Long getNextUser() {
        if (lastUserId != null) {
            for (int i = 0; i < users.size(); i++) {
                if (Objects.equals(users.get(i).id.getId(), lastUserId)) {
                    if (users.size() > i + 1) {
                        return users.get(i + 1).id.getId();
                    } else {
                        return users.get(0).id.getId();
                    }
                }
            }
        }
        
        return users.get(0).id.getId();
    }
    
}
