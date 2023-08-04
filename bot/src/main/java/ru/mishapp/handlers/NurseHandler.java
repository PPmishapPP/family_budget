package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.services.NurseService;

@TelegramHandler("настю")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class NurseHandler {

    private final NurseService nurseService;

    @TelegramCommand("приведу")
    public String leadToNurse(@TelegramParam("через") String time, Long chatId) {
        return "Время привода зафиксировано";
    }

    @TelegramCommand("заберу")
    public String takeFromNurse(@TelegramParam("через") String time, Long chatId) {
        return "Время забора зафиксировано";
    }
}
