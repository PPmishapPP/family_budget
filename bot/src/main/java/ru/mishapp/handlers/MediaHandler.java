package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.MediaDto;
import ru.mishapp.entity.Media;
import ru.mishapp.services.MediaService;

import java.util.List;
import java.util.stream.Collectors;

@TelegramHandler("медиа")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class MediaHandler {

	private final MediaService mediaService;

	@TelegramCommand("посмотреть")
	public String addMedia(@TelegramParam("название") String name,
	                       @TelegramParam("тип") String type,
	                       Long chatId) {
		Media add = mediaService.addMedia(name, type, chatId);
		return "Добавлен " + add.getType().getName() + " " + add.getName();
	}

	@TelegramCommand("список")
	public String getMedia(Long chatId) {
		List<MediaDto> mediaList = mediaService.getMediaByChatId(chatId);
		List<String> collect = mediaList.stream()
				.map(mediaItem ->
						{
							if (mediaItem.getRating() == null) {
								return String.format("%s %s %s %s",
										mediaItem.getType(),
										mediaItem.getName(),
										mediaItem.getStatus(),
										mediaItem.getDescription());
							} else {
								return String.format("%s %s %s %s %s",
										mediaItem.getType(),
										mediaItem.getName(),
										mediaItem.getStatus(),
										mediaItem.getRating(),
										mediaItem.getDescription());
							}
						}
				)
				.collect(Collectors.toList());

		return String.join("\n", collect);
	}
}
