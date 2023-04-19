package ru.mishapp.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.mishapp.KafkaTopicConfig;
import ru.mishapp.dto.Change;
import ru.mishapp.dto.KafkaMessage;
import ru.mishapp.services.ChangeService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeListener {
    
    private final ObjectMapper objectMapper;
    private final ChangeService changeService;
    private final KafkaTemplate<Long, KafkaMessage> kafkaTemplate;
    
    @KafkaListener(topics = "#{@KafkaTopicConfig.CHANGE_TOPIC}", groupId = "backConsumeGroup")
    @SneakyThrows
    public void listenCreateAccount(ConsumerRecord<Long, KafkaMessage> rec) {
        KafkaMessage request = rec.value();
        Change change = objectMapper.readValue(request.value(), Change.class);
        int balance = changeService.changeBalance(change);
        String message = String.format("Баланс у счёта %s изменён. Текущий баланс - %d₽", change.name(), balance);
        KafkaMessage response = new KafkaMessage(request.chatId(), message);
        log.info(response.toString());
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_RESPONSE_TOPIC, response);
    }
    
}
