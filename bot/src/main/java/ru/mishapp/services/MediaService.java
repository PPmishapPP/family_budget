package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.entity.Chat;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.repository.MediaRepository;

import java.util.List;

import static ru.mishapp.enumiration.MediaStatus.PLAN_TO_WATCH;

@Service
@RequiredArgsConstructor
public class MediaService {

	private final MediaRepository mediaRepository;
	private final ChatService chatService;

	@Transactional
	public Media addMedia(String name, String type, Long chatId) {
		Chat chat = chatService.getChatByChatId(chatId);
		MediaType mediaType = MediaType.of(type).orElseThrow(() -> new IllegalArgumentException(String.format("Нет типа с таким наименованием: %s", type)));
		Media media = new Media(null, name, PLAN_TO_WATCH, mediaType, chat.getId(), null, null);
		return mediaRepository.save(media);
	}

	@Transactional(readOnly = true)
	public List<Media> getMediaByChatId(Long chatId) {
		Chat chat = chatService.getChatByChatId(chatId);
		return mediaRepository.findAllByChatId(chat.getId());
	}

	@Transactional(readOnly = true)
	public List<Media> getMoviesByChatId(Long chatId) {
		return mediaRepository.findAllByChatIdAndType(chatId, MediaType.MOVIE);
	}

	@Transactional(readOnly = true)
	public List<Media> getSeriesByChatId(Long chatId) {
		return mediaRepository.findAllByChatIdAndType(chatId, MediaType.SERIES);
	}
}
