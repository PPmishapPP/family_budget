package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;

@Table("media")
@Getter
public class Media {

	@Id
	private final Long id;
	@Nonnull
	private final String name;
	@Nonnull
	private final MediaStatus status;
	@Nonnull
	private final MediaType type;

	private final long chatId;

	private final Integer rating;
	private final String description;

	public Media(Long id, @Nonnull String name, @Nonnull MediaStatus status, @Nonnull MediaType type, long chatId, Integer rating, String description) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.type = type;
		this.chatId = chatId;
		this.rating = rating;
		this.description = description;
	}
}
