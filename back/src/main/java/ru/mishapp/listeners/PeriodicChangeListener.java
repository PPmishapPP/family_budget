package ru.mishapp.listeners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.mishapp.KafkaTopicConfig;
import ru.mishapp.dto.KafkaMessage;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.services.PeriodicChangeService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodicChangeListener {
    
    private final PeriodicChangeService periodicChangeService;
    private final KafkaTemplate<Long, KafkaMessage> kafkaTemplate;
    
    @KafkaListener(topics = "#{@KafkaTopicConfig.PERIODIC_CHANGE_CREATE_TOPIC}", groupId = "backConsumeGroup")
    @SneakyThrows
    public void listenCreateAccount(ConsumerRecord<Long, KafkaMessage> rec) {
        KafkaMessage request = rec.value();
        PeriodicChange periodicChange = periodicChangeService.create(request.value(), request.chatId());
        KafkaMessage response = new KafkaMessage(request.chatId(), "Создано периодическое изменение - " + periodicChange.toTelegram());
        log.info(response.toString());
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_RESPONSE_TOPIC, response);
    }
}
