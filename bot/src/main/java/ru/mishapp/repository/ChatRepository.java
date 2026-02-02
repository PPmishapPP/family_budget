package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.Chat;

import java.util.Optional;

public interface ChatRepository extends CrudRepository<Chat, Long> {

	Optional<Chat> findByChatId(Long chatId);

	Optional<Chat> findByName(String name);

}
