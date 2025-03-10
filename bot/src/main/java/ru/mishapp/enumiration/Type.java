package ru.mishapp.enumiration;

import jakarta.annotation.Nullable;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.BiFunction;

@Getter
public enum Type {
	MONTHLY("Ежемесячно", LocalDate::plusMonths),
	WEEKLY("Еженедельно", LocalDate::plusWeeks),
	DAILY("Ежедневно", LocalDate::plusDays),
	ONE("Единоразово", (d, i) -> null),
	YEARLY("Ежегодно", LocalDate::plusYears);

	private final String description;
	private final BiFunction<LocalDate, Integer, LocalDate> next;


	Type(String description, BiFunction<LocalDate, Integer, LocalDate> next) {
		this.description = description;
		this.next = next;
	}

	public static Optional<Type> of(String type) {
		for (Type value : values()) {
			if (value.description.equals(type)) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}

	@Nullable
	public LocalDate next(LocalDate day, int pass) {
		return next.apply(day, pass + 1);
	}
}