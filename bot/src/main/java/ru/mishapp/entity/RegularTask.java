package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import ru.mishapp.enumiration.Type;

import java.time.LocalDateTime;

@Table("regular_tasks")
@Getter
@Builder
@With
public class RegularTask {
	@Id
	private final Long id;
	@Nonnull
	private final String name;
	@Nonnull
	private final Type type;
	@Nonnull
	private final LocalDateTime startDate;
	@Nonnull
	private final String description;
	private final int pass;
	private final long chatId;
	@Nonnull
	private final LocalDateTime datetimeNotification;

	@PersistenceCreator
	public RegularTask(Long id, @Nonnull String name, Type type, LocalDateTime startDate, String description, int pass, long chatId, LocalDateTime datetimeNotification) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.startDate = startDate;
		this.description = description;
		this.pass = pass;
		this.chatId = chatId;
		this.datetimeNotification = datetimeNotification;
	}

	public RegularTask(@Nonnull String name, Type type, LocalDateTime startDate, String description,  int pass, long chatId, LocalDateTime datetimeNotification) {
		this(null, name, type, startDate, description, pass,chatId, datetimeNotification);
	}

	public String toTelegram() {
		return name;
	}
}
