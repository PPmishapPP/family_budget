package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaType;

import java.util.List;

public interface MediaRepository extends CrudRepository<Media, Long> {

	List<Media> findAllByChatId(Long chatId);
	List<Media> findAllByChatIdAndType(Long chatId, MediaType type);
}
