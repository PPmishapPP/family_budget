package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.AccountReadDTO;

import java.util.concurrent.CompletableFuture;

@TelegramHandler("account")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class AccountHandler {
    
    private final KafkaTemplate<Long, Object> kafkaTemplate;
    
    @TelegramCommand()
    public String readAll() {
        return "У вас очень-очень много денег!";
    }
    
    
    @TelegramCommand("read")
    public void readByName(@TelegramParam("name") String name) {
        AccountReadDTO accountReadDTO = new AccountReadDTO(name);
        CompletableFuture<SendResult<Long, Object>> future = kafkaTemplate.send("accountRequestTopic", accountReadDTO);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[" + accountReadDTO +
                    "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                log.info("Unable to send message=[" +
                    accountReadDTO + "] due to : " + ex.getMessage());
            }
        });
    }
}
