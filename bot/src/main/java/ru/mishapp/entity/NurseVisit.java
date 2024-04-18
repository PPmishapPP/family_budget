package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Table("nurse_visit")
@Getter
public class NurseVisit {

    @Id
    private final Long id;
    @Nonnull
    private final LocalDate visitDay;

    private final long chatId;

    private final LocalTime visitStart;

    private LocalTime visitEnd;

    @PersistenceCreator
    public NurseVisit(Long id, @Nonnull LocalDate visitDay, long chatId, LocalTime visitStart, LocalTime visitEnd) {
        this.id = id;
        this.visitDay = visitDay;
        this.chatId = chatId;
        this.visitStart = visitStart;
        this.visitEnd = visitEnd;
    }

    public NurseVisit(LocalDate visitDay, long chatId, LocalTime visitStart) {
        this(null, visitDay, chatId, visitStart, null);
    }

    public void setVisitEnd(LocalTime visitEnd) {
        this.visitEnd = visitEnd;
    }
}
