package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.entity.Media;
import ru.mishapp.services.MediaService;

import java.util.List;

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
		List<Media> mediaList = mediaService.getMediaByChatId(chatId);
		StringBuilder builder = new StringBuilder();
		for (Media media : mediaList) {
			builder.append(media.getType().getName());
			builder.append(" ");
			builder.append("'");
			builder.append(media.getName());
			builder.append("'");
			builder.append(" ");
			builder.append("статус");
			builder.append(" ");
			builder.append(media.getStatus().getName());
			if (media.getRating() != null) {
				builder.append(" ");
				builder.append(media.getRating());
			}
			if (media.getRating() != null) {
				builder.append(" ");
				builder.append(media.getDescription());
			}
			builder.append("\n\n");
		}
		return builder.toString();
	}
}
