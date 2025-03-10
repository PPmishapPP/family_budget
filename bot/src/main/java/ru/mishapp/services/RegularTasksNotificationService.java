package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.Notification;
import ru.mishapp.repository.RegularTasksRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegularTasksNotificationService {

	private final RegularTasksRepository regularTasksRepository;

	private static final LocalTime START_OF_DAY = LocalTime.of(12, 0);
	private static final LocalTime END_OF_DAY = LocalTime.of(22, 0);

	public List<Notification> getActualNotifications() {
		List<Notification> notificationsToSend = new ArrayList<>();
		LocalTime timeNow = LocalTime.now();
		if (timeNow.isAfter(START_OF_DAY) && timeNow.isBefore(END_OF_DAY)) {
			regularTasksRepository.findAll().forEach(task -> {
				LocalDateTime notificationDatetime = task.getDatetimeNotification();
				long chatId = task.getChatId();
				LocalDateTime now = LocalDateTime.now();
				if (now.isAfter(notificationDatetime)) {
					notificationsToSend.add(createNotification(chatId, task.getDescription()));
				}
			});
		}

		return notificationsToSend;
	}

	private Notification createNotification(long chatId, String description) {
		return new Notification(chatId, description);
	}
}
