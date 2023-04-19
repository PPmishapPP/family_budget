package ru.mishapp.listners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.mishapp.Bot;
import ru.mishapp.dto.KafkaMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountListener {
    
    private final Bot bot;
    
    @KafkaListener(topics = "#{@KafkaTopicConfig.ACCOUNT_RESPONSE_TOPIC}", groupId = "botConsumeGroup")
    @SneakyThrows
    public void listenAccount(ConsumerRecord<Long, KafkaMessage> rec) {
        KafkaMessage response = rec.value();
        bot.execute(new SendMessage(response.chatId().toString(), response.value()));
    }
}
