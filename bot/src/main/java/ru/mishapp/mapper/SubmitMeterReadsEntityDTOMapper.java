package ru.mishapp.mapper;

import org.mapstruct.Mapper;
import ru.mishapp.dto.SubmitMeterReadsDTO;
import ru.mishapp.entity.SubmitMeterReads;

@Mapper(
        componentModel = "spring"
)
public interface SubmitMeterReadsEntityDTOMapper {

    SubmitMeterReadsDTO entityToDTO(SubmitMeterReads submitMeterReads);

    SubmitMeterReads dtoToEntity(SubmitMeterReadsDTO submitMeterReadsDTO);
}
