package ru.mishapp.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.mishapp.KafkaTopicConfig;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.Change;
import ru.mishapp.dto.KafkaMessage;
import ru.mishapp.services.KafkaSendService;

@TelegramHandler("change")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ChangeHandler {
    
    private final KafkaSendService kafkaSendService;
    private final ObjectMapper objectMapper;
    
    @TelegramCommand("add")
    @SneakyThrows
    public void add(
        @TelegramParam("name") String accountName,
        @TelegramParam("sum") String sum,
        @TelegramParam("comment") String comment,
        Long chatId
    ) {
        String value = objectMapper.writeValueAsString(
            new Change(accountName, Integer.parseInt(sum.replace(" ", "")), comment)
        );
        kafkaSendService.send(new KafkaMessage(chatId, value), KafkaTopicConfig.CHANGE_TOPIC);
    }
    
    @TelegramCommand("delete")
    @SneakyThrows
    public void delete(
        @TelegramParam("name") String accountName,
        @TelegramParam("sum") String sum,
        @TelegramParam("comment") String comment,
        Long chatId
    ) {
        String value = objectMapper.writeValueAsString(
            new Change(accountName, -1 * Integer.parseInt(sum), comment)
        );
        kafkaSendService.send(new KafkaMessage(chatId, value), KafkaTopicConfig.CHANGE_TOPIC);
    }
}
