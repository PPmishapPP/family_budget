package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import ru.mishapp.KafkaTopicConfig;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.KafkaMessage;
import ru.mishapp.services.KafkaSendService;

@TelegramHandler("periodic_change")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PeriodicChangeHandler {
    
    private final KafkaSendService kafkaSendService;
    
    @TelegramCommand("create")
    public void create(@TelegramParam("name") String name, Long chatId) {
        kafkaSendService.send(new KafkaMessage(chatId, name), KafkaTopicConfig.PERIODIC_CHANGE_CREATE_TOPIC);
    }
}
