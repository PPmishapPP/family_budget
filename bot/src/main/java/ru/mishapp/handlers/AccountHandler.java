package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.KafkaMessage;

import java.util.concurrent.CompletableFuture;

@TelegramHandler("account")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class AccountHandler {
    
    private final KafkaTemplate<Long, KafkaMessage> kafkaTemplate;
    
    @TelegramCommand()
    public String readAll(Long chatId) {
        return "У вас очень-очень много денег!";
    }
    
    
    @TelegramCommand("read")
    public void readByName(@TelegramParam("name") String name, Long chatId) {
        KafkaMessage kafkaMessage = new KafkaMessage(chatId, name);
        CompletableFuture<SendResult<Long, KafkaMessage>> future = kafkaTemplate.send("accountRequestTopic", kafkaMessage);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[" + name +
                    "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                log.info("Unable to send message=[" +
                    name + "] due to : " + ex.getMessage());
            }
        });
    }
}
