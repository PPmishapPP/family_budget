package ru.mishapp.api.mapper;

import org.springframework.stereotype.Component;
import ru.mishapp.api.dto.MediaStatusDto;
import ru.mishapp.api.dto.MediaTypeDto;
import ru.mishapp.api.dto.PaymentTypeDto;
import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.enumiration.Type;

import java.util.Arrays;
import java.util.List;

@Component
public class ReferenceApiMapper {

    public List<PaymentTypeDto> paymentTypes() {
        return Arrays.stream(Type.values())
                .map(type -> new PaymentTypeDto(type.name(), type.getDescription()))
                .toList();
    }

    public List<MediaTypeDto> mediaTypes() {
        return Arrays.stream(MediaType.values())
                .map(type -> new MediaTypeDto(type.name(), type.getName()))
                .toList();
    }

    public List<MediaStatusDto> mediaStatuses() {
        return Arrays.stream(MediaStatus.values())
                .map(status -> new MediaStatusDto(
                        status.name(),
                        status.getName(),
                        status.getAvailableTransitions().stream().map(Enum::name).toList()))
                .toList();
    }
}
