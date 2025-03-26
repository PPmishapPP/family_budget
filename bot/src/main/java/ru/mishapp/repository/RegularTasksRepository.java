package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.RegularTask;

import java.util.Optional;

public interface RegularTasksRepository extends CrudRepository<RegularTask, Long> {
	Optional<RegularTask> findByNameAndChatId(String name, long chatId);
}
