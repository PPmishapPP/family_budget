package ru.mishapp.dto;

import java.time.LocalDate;

public record PeriodicChangeRuleDTO(
    String name,
    String pcName,
    String taName,
    String recName,
    int sum,
    String type,
    int pass,
    LocalDate startDay
) {

}
