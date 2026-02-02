package ru.mishapp.enumiration;

import lombok.Getter;

@Getter
public enum MediaStatus {
	PLAN_TO_WATCH("Ожидает просмотра"),
	WATCHED("Просмотрено"),
	WAITING_FOR_CONTINUATION("Ожидает продолжения"),
	STOPPED("Остановлен просмотр");

	private final String name;

	MediaStatus(String name) {
		this.name = name;
	}
}
