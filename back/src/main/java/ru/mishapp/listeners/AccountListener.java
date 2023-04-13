package ru.mishapp.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.KafkaMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountListener {
    
    private final KafkaTemplate<Long, KafkaMessage> kafkaTemplate;
    
    @KafkaListener(topics = "accountRequestTopic", groupId = "backConsumeGroup")
    public void listenAccount(ConsumerRecord<String, KafkaMessage> rec) {
        KafkaMessage request = rec.value();
        KafkaMessage response = new KafkaMessage(request.chatId(), "На счету " + request.value() + " очень много денег (кафка работает)");
        log.info(response.toString());
        kafkaTemplate.send("accountResponseTopic", response);
    }
}
