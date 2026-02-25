package ru.mishapp.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.mishapp.enumiration.MediaStatus;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public final class MediaDto {
	private Long id;
	private String name;
	private String type;
	private MediaStatus status;
	private Integer rating;
	private String description;
}