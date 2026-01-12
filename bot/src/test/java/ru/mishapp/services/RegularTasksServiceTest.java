package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.entity.RegularTask;
import ru.mishapp.repository.RegularTasksRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.mishapp.enumiration.Type.MONTHLY;

@ExtendWith(MockitoExtension.class)
class RegularTasksServiceTest {

	@InjectMocks
	RegularTasksService regularTasksService;

	@Mock
	RegularTasksRepository regularTasksRepository;

	@Mock
	DateTimeService dateTimeService;

	/**
	 * Перенос даты уведомления в рамках текущего дня
	 * Ожидается, что время перенесется корректно относительно текущего времени
	 */
	@Test
	public void test() {
		RegularTask regularTask = new RegularTask("Показания", MONTHLY, LocalDateTime.of(2025, 3, 10, 12, 0), "", 0, 1L,
				LocalDateTime.of(2026, 1, 23, 12, 0));
		when(regularTasksRepository.findByNameAndChatId("Показания", 1L)).thenReturn(Optional.of(regularTask));
		when(dateTimeService.getCurrentDateTime()).thenReturn(LocalDateTime.of(2026, 1, 23, 15, 0));
		ArgumentCaptor<RegularTask> regularTaskArgumentCaptor = ArgumentCaptor.forClass(RegularTask.class);
		regularTasksService.putOff("Показания", "3", 1L);

		verify(regularTasksRepository).save(regularTaskArgumentCaptor.capture());
		RegularTask savedTask = regularTaskArgumentCaptor.getValue();
		assertEquals(LocalDateTime.of(2026, 1, 23, 18, 0), savedTask.getDatetimeNotification());
	}

	/**
	 * Выполняется перенос даты уведомления на следующий день после начала уведомлений
	 * Ожидается, что новая дата уведомления будет с текущей датой
	 */
	@Test
	public void test_2() {
		RegularTask regularTask = new RegularTask("Показания", MONTHLY, LocalDateTime.of(2025, 3, 10, 12, 0), "", 0, 1L,
				LocalDateTime.of(2026, 1, 23, 12, 0));
		when(regularTasksRepository.findByNameAndChatId("Показания", 1L)).thenReturn(Optional.of(regularTask));
		when(dateTimeService.getCurrentDateTime()).thenReturn(LocalDateTime.of(2026, 1, 24, 15, 0));
		ArgumentCaptor<RegularTask> regularTaskArgumentCaptor = ArgumentCaptor.forClass(RegularTask.class);
		regularTasksService.putOff("Показания", "3", 1L);

		verify(regularTasksRepository).save(regularTaskArgumentCaptor.capture());
		RegularTask savedTask = regularTaskArgumentCaptor.getValue();
		assertEquals(LocalDateTime.of(2026, 1, 24, 18, 0), savedTask.getDatetimeNotification());
	}

	/**
	 * Перенос даты уведомления в рамках текущего дня
	 * Ожидается, что текущие минуты и секунды не влияют на новую дату уведомления
	 */
	@Test
	public void test_3() {
		RegularTask regularTask = new RegularTask("Показания", MONTHLY, LocalDateTime.of(2025, 3, 10, 12, 0), "", 0, 1L,
				LocalDateTime.of(2026, 1, 23, 12, 0));
		when(regularTasksRepository.findByNameAndChatId("Показания", 1L)).thenReturn(Optional.of(regularTask));
		when(dateTimeService.getCurrentDateTime()).thenReturn(LocalDateTime.of(2026, 1, 23, 15, 12, 36));
		ArgumentCaptor<RegularTask> regularTaskArgumentCaptor = ArgumentCaptor.forClass(RegularTask.class);
		regularTasksService.putOff("Показания", "3", 1L);

		verify(regularTasksRepository).save(regularTaskArgumentCaptor.capture());
		RegularTask savedTask = regularTaskArgumentCaptor.getValue();
		assertEquals(LocalDateTime.of(2026, 1, 23, 18, 0, 0), savedTask.getDatetimeNotification());
	}
}