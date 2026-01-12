package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.RegularTask;
import ru.mishapp.repository.RegularTasksRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegularTasksService {

	private final RegularTasksRepository regularTasksRepository;
	private final DateTimeService dateTimeService;

	public String done(String name, Long chatId) {
		RegularTask regularTask = regularTasksRepository.findByNameAndChatId(name, chatId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Нет задачи с таким именем: %s", name)));
		LocalDateTime datetimeNotification = dateTimeService.createNewNotificationDate(regularTask);
		RegularTask newRegularTask = regularTask.withDatetimeNotification(datetimeNotification);
		regularTasksRepository.save(newRegularTask);
		return String.format("Задание %s перенесено на %s", regularTask.getName(), newRegularTask.getDatetimeNotification());
	}

	public String putOff(String name, String hours, Long chatId) {
		RegularTask regularTask = regularTasksRepository.findByNameAndChatId(name, chatId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Нет задачи с таким именем: %s", name)));
		LocalDateTime newTime = dateTimeService.getCurrentDateTime().plusHours(Long.parseLong(hours)).withMinute(0).withSecond(0);
		RegularTask newRegularTask = regularTask.withDatetimeNotification(newTime);
		regularTasksRepository.save(newRegularTask);
		return String.format("Задание %s перенесено на %s", newRegularTask.getName(), newRegularTask.getDatetimeNotification());
	}
}
