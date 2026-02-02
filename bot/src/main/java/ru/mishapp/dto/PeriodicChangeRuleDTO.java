package ru.mishapp.dto;

import java.time.LocalDate;

public record PeriodicChangeRuleDTO(
    String name,
    String pcName,
    String taName,
    int sum,
    String type,
    int pass,
    LocalDate startDay,
	boolean isActive,
    LocalDate endDate
) {

}
