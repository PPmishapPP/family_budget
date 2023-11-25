package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.services.NurseService;

import java.time.LocalDate;
import java.time.LocalTime;

@TelegramHandler("настю")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class NurseHandler {

    private final NurseService nurseService;

    @TelegramCommand("приведу")
    public String leadToNurse(@TelegramParam("в") String time, Long chatId) {
        LocalDate visitDay = LocalDate.now();
        LocalTime visitStart = LocalTime.parse(time);
        nurseService.fixedStartNurseVisit(visitDay, visitStart, chatId);
        return "Настю привели в " + visitStart.toString();
    }

    @TelegramCommand("заберу")
    public String takeFromNurse(@TelegramParam("в") String time, Long chatId) {
        LocalDate visitDay = LocalDate.now();
        LocalTime visitEnd = LocalTime.parse(time);
        return nurseService.endNurseVisit(visitDay, visitEnd, chatId);
    }
}
