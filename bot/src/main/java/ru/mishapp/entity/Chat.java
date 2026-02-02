package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Table("chat")
@Getter
public class Chat {
	@Id
	private final Long id;
	@Nonnull
	private final String name;
	@Nonnull
	private final Long chatId;

	@MappedCollection(idColumn = "chat_id")
	private final Set<Media> media;

	public Chat(Long id, @Nonnull String name, @NotNull Long chatId, Set<Media> media) {
		this.id = id;
		this.name = name;
		this.chatId = chatId;
		this.media = media;
	}
}
