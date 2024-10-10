package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("submit_meter_reads")
@Getter
public class SubmitMeterReads {

    @Id
    private final Long id;

    @Nonnull
    private final Long chatId;

    @Nonnull
    private final LocalDateTime datetimeOfSubmitMeterReads;

    @PersistenceCreator
    public SubmitMeterReads(
            Long id,
            Long chatId,
            LocalDateTime datetimeOfSubmitMeterReads
    ) {
        this.id = id;
        this.chatId = chatId;
        this.datetimeOfSubmitMeterReads = datetimeOfSubmitMeterReads;
    }

}
