package ru.mishapp.dto;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
public class SubmitMeterReadsDTO {

    private Long id;

    @Nonnull
    private Long chatId;

    @Nonnull
    private LocalDateTime datetimeOfSubmitMeterReads;
}
