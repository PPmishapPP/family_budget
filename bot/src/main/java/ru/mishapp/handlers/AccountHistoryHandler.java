package ru.mishapp.handlers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mishapp.KafkaTopicConfig;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.KafkaMessage;
import ru.mishapp.services.KafkaSendService;

@TelegramHandler("account_history")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class AccountHistoryHandler {
    
    
    private final KafkaSendService kafkaSendService;
    
    
    @TelegramCommand()
    public void readAll(Long chatId) {
        kafkaSendService.send(new KafkaMessage(chatId, ""), KafkaTopicConfig.ACCOUNT_HISTORY_READ_TOPIC);
    }
    
    
    @TelegramCommand("read")
    public void readByName(@TelegramParam("name") String name, Long chatId) {
        kafkaSendService.send(new KafkaMessage(chatId, name), KafkaTopicConfig.ACCOUNT_HISTORY_READ_TOPIC);
    }
    
}
