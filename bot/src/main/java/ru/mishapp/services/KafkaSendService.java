package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.KafkaMessage;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaSendService {
    
    private final KafkaTemplate<Long, KafkaMessage> kafkaTemplate;
    
    
    public void send(KafkaMessage kafkaMessage, String topic) {
        CompletableFuture<SendResult<Long, KafkaMessage>> future = kafkaTemplate.send(topic, kafkaMessage);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[" + kafkaMessage.value() +
                    "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                log.error("Unable to send message=[" +
                    kafkaMessage.value() + "] due to : " + ex.getMessage());
            }
        });
    }
}
