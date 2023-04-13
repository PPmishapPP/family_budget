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
    
    @KafkaListener(topics = "accountRequestTopic", groupId = "botConsumeGroup")
    @SneakyThrows
    public void listenAccount(ConsumerRecord<String, KafkaMessage> rec) {
        KafkaMessage request = rec.value();
        KafkaMessage response = new KafkaMessage(request.chatId(), "На счету " + request.value() + " очень много денег (кафка работает)");
        log.info(response.toString());
        bot.execute(new SendMessage(response.chatId().toString(), response.value()));
    }
}
