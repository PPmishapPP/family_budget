package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.dto.MediaDto;
import ru.mishapp.entity.Chat;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.mapper.MediaMapper;
import ru.mishapp.repository.MediaRepository;

import java.util.List;

import static ru.mishapp.enumiration.MediaStatus.PLAN_TO_WATCH;

@Service
@RequiredArgsConstructor
public class MediaService {

	private final MediaRepository mediaRepository;
	private final ChatService chatService;
	private final MediaMapper mapper;

	@Transactional
	public Media addMedia(String name, String type, Long chatId) {
		Chat chat = chatService.getChatByChatId(chatId);
		MediaType mediaType = MediaType.of(type).orElseThrow(() -> new IllegalArgumentException(String.format("Нет типа с таким наименованием: %s", type)));
		Media media = new Media(null, name, PLAN_TO_WATCH, mediaType, chat.getId(), null, null);
		return mediaRepository.save(media);
	}

	@Transactional
	public void saveMedia(MediaDto mediaDto, Long chatId) {
		Chat chat = chatService.getChatByChatId(chatId);
		Media media = mapper.toEntity(mediaDto);
		media.setChatId(chat.getId());
		mediaRepository.save(media);
	}

	@Transactional(readOnly = true)
	public List<MediaDto> getMediaByChatId(Long chatId) {
		Chat chat = chatService.getChatByChatId(chatId);
		List<Media> mediaList = mediaRepository.findAllByChatId(chat.getId());
		return mapper.toDtoList(mediaList);
	}

	@Transactional(readOnly = true)
	public List<MediaDto> getMoviesByChatId(Long chatId) {
		List<Media> allByChatIdAndType = mediaRepository.findAllByChatIdAndType(chatId, MediaType.MOVIE);
		return mapper.toDtoList(allByChatIdAndType);
	}

	@Transactional(readOnly = true)
	public List<MediaDto> getSeriesByChatId(Long chatId) {
		List<Media> allByChatIdAndType = mediaRepository.findAllByChatIdAndType(chatId, MediaType.SERIES);
		return mapper.toDtoList(allByChatIdAndType);
	}

	public void deleteMediaItem(Long id) {
		mediaRepository.deleteById(id);
	}
}
