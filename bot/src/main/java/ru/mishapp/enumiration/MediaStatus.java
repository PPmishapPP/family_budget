package ru.mishapp.enumiration;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public enum MediaStatus {
	PLAN_TO_WATCH("Ожидает просмотра"),
	IN_PROGRESS("В процессе"),
	WATCHED("Просмотрено"),
	WAITING_FOR_CONTINUATION("Ожидает продолжения"),
	STOPPED("Остановлен просмотр"),
	CANCELED("Отмена просмотра");

	private final String name;

	MediaStatus(String name) {
		this.name = name;
	}

	public static MediaStatus of(String statusName) {
		for (MediaStatus type : values()) {
			if (type.name.toLowerCase().equals(statusName)) {
				return type;
			}
		}
		throw new IllegalArgumentException(String.format("Нет статуса с name = '%s'", statusName));
	}

	private static final Map<MediaStatus, List<MediaStatus>> AVAILABLE_TRANSITIONS =
			Map.of(
					PLAN_TO_WATCH, List.of(
							IN_PROGRESS, WATCHED, WAITING_FOR_CONTINUATION, CANCELED
					),
					WATCHED, List.of(IN_PROGRESS, WAITING_FOR_CONTINUATION),
					WAITING_FOR_CONTINUATION, List.of(
							IN_PROGRESS, WATCHED, STOPPED, CANCELED
					),
					STOPPED, List.of(
							IN_PROGRESS, PLAN_TO_WATCH, WATCHED, WAITING_FOR_CONTINUATION, CANCELED
					),
					CANCELED, List.of(IN_PROGRESS, PLAN_TO_WATCH)
			);

	public List<MediaStatus> getAvailableTransitions() {
		return AVAILABLE_TRANSITIONS.getOrDefault(this, Arrays.stream(MediaStatus.values()).toList());
	}
}
