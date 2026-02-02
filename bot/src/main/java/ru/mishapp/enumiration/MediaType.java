package ru.mishapp.enumiration;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum MediaType {
	SERIES("Сериал"),
	MOVIE("Фильм");

	private final String name;

	MediaType(String name) {
		this.name = name;
	}

	public static Optional<MediaType> of(String mediaType) {
		for (MediaType type : values()) {
			if (type.name.toLowerCase().equals(mediaType)) {
				return Optional.of(type);
			}
		}
		return Optional.empty();
	}
}
