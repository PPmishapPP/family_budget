package ru.mishapp.api.mapper;

import org.springframework.stereotype.Component;
import ru.mishapp.api.dto.MediaDto;
import ru.mishapp.api.dto.MediaRequest;
import ru.mishapp.entity.Media;
import ru.mishapp.services.records.MediaCommand;

@Component
public class MediaApiMapper {

    public MediaCommand toCommand(MediaRequest request) {
        return new MediaCommand(
                request.name(),
                request.type(),
                request.status(),
                request.rating(),
                request.description()
        );
    }

    public MediaDto toDto(Media media) {
        return new MediaDto(
                media.getId(),
                media.getName(),
                media.getType(),
                media.getStatus(),
                media.getRating(),
                media.getDescription()
        );
    }
}
