package ru.mishapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mishapp.dto.MediaDto;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaType;

import java.util.List;

@Mapper(componentModel = "spring", imports = MediaType.class)
public interface MediaMapper {

	@Mapping(target = "type", source = "type.name")
	MediaDto toDto(Media media);

	List<MediaDto> toDtoList(List<Media> mediaList);

	@Mapping(target = "chatId", ignore = true)
	@Mapping(target = "type", expression = "java(MediaType.of(mediaDto.getType()).get())")
	Media toEntity(MediaDto mediaDto);
}