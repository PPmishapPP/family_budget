package ru.mishapp.dto;

import lombok.With;

import java.time.LocalDate;

@With
public record PeriodicChangeRuleDto(
		Long id,
		String name,
		String periodicChangeName,
		String targetAccountName,
		int sum,
		String type,
		int pass,
		LocalDate nextDay,
		boolean active,
		LocalDate endDate
) {

}
