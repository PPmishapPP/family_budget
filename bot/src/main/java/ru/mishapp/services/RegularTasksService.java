package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.RegularTask;
import ru.mishapp.enumiration.Type;
import ru.mishapp.repository.RegularTasksRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class RegularTasksService {

	private final RegularTasksRepository regularTasksRepository;

	public String done(String name, Long chatId) {
		RegularTask regularTask = regularTasksRepository.findByNameAndChatId(name, chatId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Нет задачи с таким именем: %s", name)));
		LocalDateTime datetimeNotification = createNewNotificationDate(regularTask);
		RegularTask newRegularTask = regularTask.withDatetimeNotification(datetimeNotification);
		regularTasksRepository.save(newRegularTask);
		return String.format("Задание %s перенесено на %s", regularTask.getName(), newRegularTask.getDatetimeNotification());
	}

	private LocalDateTime createNewNotificationDate(RegularTask regularTask) {
		Type type = regularTask.getType();
		LocalDate now = LocalDate.now();
		LocalDate newNotificationDate = regularTask.getStartDate().toLocalDate();
		while (newNotificationDate.isBefore(now) || newNotificationDate.equals(now)) {
			newNotificationDate = type.next(newNotificationDate, regularTask.getPass());
		}

		return LocalDateTime.of(newNotificationDate, LocalTime.NOON);
	}

	public String putOff(String name, String hours, Long chatId) {
		RegularTask regularTask = regularTasksRepository.findByNameAndChatId(name, chatId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Нет задачи с таким именем: %s", name)));
		LocalDateTime datetimeNotification = regularTask.getDatetimeNotification();
		LocalDateTime newTime = datetimeNotification.plusHours(Long.parseLong(hours));
		RegularTask newRegularTask = regularTask.withDatetimeNotification(newTime);
		regularTasksRepository.save(newRegularTask);
		return String.format("Задание %s перенесено на %s", newRegularTask.getName(), newRegularTask.getDatetimeNotification());
	}
}
