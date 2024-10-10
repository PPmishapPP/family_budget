package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.dto.SubmitMeterReadsDTO;
import ru.mishapp.entity.SubmitMeterReads;
import ru.mishapp.mapper.SubmitMeterReadsEntityDTOMapper;
import ru.mishapp.repository.SubmitMeterReadsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SubmitMeterReadsService {

    private final SubmitMeterReadsRepository submitMeterReadsRepository;
    private final SubmitMeterReadsEntityDTOMapper mapper;

    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final String MESSAGE_METERS_SUBMITTED = "Показания сданы." +
            "\nВремя сдачи показаний перенесено на %s";
    public static final String MESSAGE_NUMBER_NOT_FOUND_IN_STRING = "Число не найдено в строке.";

    @Transactional
    public String setDatetimeOfSubmitMeterReads(Long chatId) {
        String nextMeterSubmitTerm = rescheduleMetersSubmitToNextMonth(chatId);

        return String.format(MESSAGE_METERS_SUBMITTED, nextMeterSubmitTerm);
    }

    @Transactional
    public String postponeMeterSubmit(String hours, Long chatId) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(hours);

        int hoursInt;

        if (matcher.find()) {
            hoursInt = Integer.parseInt(matcher.group(1));
        } else {
            return MESSAGE_NUMBER_NOT_FOUND_IN_STRING;
        }

        return rescheduleMetersSubmitBySpecifiedHours(chatId, hoursInt);
    }

    public String rescheduleMetersSubmitToNextMonth(Long chatId) {
        LocalDateTime nowDatetime = LocalDateTime.now();
        LocalDateTime rescheduledDatetime = LocalDateTime.of(
                nowDatetime.getYear(),
                nowDatetime.getMonth().plus(1),
                23,
                12,
                0
        );
        return putNextMetersSubmitTermIntoDB(chatId, rescheduledDatetime);
    }

    public String rescheduleMetersSubmitBySpecifiedHours(
            Long chatId,
            int hours
    ) {
        LocalDateTime rescheduledDatetime = LocalDateTime.now().plusHours(hours);
        return putNextMetersSubmitTermIntoDB(chatId, rescheduledDatetime);
    }

    private String putNextMetersSubmitTermIntoDB(
            Long chatId,
            LocalDateTime datetime
    ) {
        SubmitMeterReadsDTO submitMeterReadsDTO = mapper.entityToDTO(readByChatId(chatId));
        submitMeterReadsDTO.setDatetimeOfSubmitMeterReads(datetime);

        SubmitMeterReads submitMeterReads = submitMeterReadsRepository.save(
                mapper.dtoToEntity(submitMeterReadsDTO)
        );

        return submitMeterReads.getDatetimeOfSubmitMeterReads().format(formatter);
    }

    private SubmitMeterReads readByChatId(Long chatId) {
        return submitMeterReadsRepository.readByChatId(chatId);
    }

}
