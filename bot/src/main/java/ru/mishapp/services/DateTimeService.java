package ru.mishapp.services;

import org.springframework.stereotype.Service;
import ru.mishapp.entity.RegularTask;
import ru.mishapp.enumiration.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DateTimeService {

	public LocalDateTime createNewNotificationDate(RegularTask regularTask) {
		Type type = regularTask.getType();
		LocalDate now = LocalDate.now();
		LocalDate newNotificationDate = regularTask.getStartDate().toLocalDate();
		while (newNotificationDate.isBefore(now) || newNotificationDate.equals(now)) {
			newNotificationDate = type.next(newNotificationDate, regularTask.getPass());
		}

		return LocalDateTime.of(newNotificationDate, LocalTime.NOON);
	}

	public LocalDateTime getCurrentDateTime() {
		return LocalDateTime.now();
	}
}
